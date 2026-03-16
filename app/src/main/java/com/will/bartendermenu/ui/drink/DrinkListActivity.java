package com.will.bartendermenu.ui.drink;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.will.bartendermenu.model.Drink;
import com.will.bartendermenu.R;
import com.will.bartendermenu.database.DatabaseHelper;
import com.will.bartendermenu.ui.menu.MenuListActivity;
import com.will.bartendermenu.model.Menu;
import com.will.bartendermenu.ui.menu.MenuSelectionAdapter;

import java.util.List;
import android.widget.ImageButton;

import android.content.SharedPreferences;
import android.widget.EditText;

public class DrinkListActivity extends AppCompatActivity implements DrinkAdapter.OnDrinkClickListener {

    private RecyclerView recyclerView;
    private DrinkAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Drink> drinkList;

    private static final int REQUEST_EDIT_DRINK = 100;
    private static final String PREFS = "bartender_prefs";
    private static final String KEY_CONTRACTOR_NAMES = "contractor_names";

    private EditText editTextContractorNames;
    private Button btnSaveContractor;
    private Button btnReorderTasting;
    private Button btnOpenMenus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_list);

        // 🔹 Views principais
        editTextContractorNames = findViewById(R.id.editTextContractorNames);
        btnSaveContractor = findViewById(R.id.btnSaveContractor);
        btnReorderTasting = findViewById(R.id.btnReorderTasting);
        Button btnAddNewDrink = findViewById(R.id.btnAddNewDrink);
        recyclerView = findViewById(R.id.recyclerViewDrinks);

        btnOpenMenus = findViewById(R.id.btnOpenMenus);

        btnOpenMenus.setOnClickListener(v -> {
            Intent intent = new Intent(DrinkListActivity.this, MenuListActivity.class);
            startActivity(intent);
        });

        // 🔙 botão back do include
        View includeView = findViewById(R.id.includeTopBack);
        ImageButton btnBackTop = null;

        if (includeView != null) {
            btnBackTop = includeView.findViewById(R.id.btnBackTop);
        } else {
            btnBackTop = findViewById(R.id.btnBackTop);
        }

        if (btnBackTop != null) {
            btnBackTop.setOnClickListener(v -> onBackPressed());
        }

        // 🔹 SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = prefs.getString(KEY_CONTRACTOR_NAMES, "");

        if (editTextContractorNames != null) {
            editTextContractorNames.setText(saved);
        }

        if (btnSaveContractor != null && editTextContractorNames != null) {
            btnSaveContractor.setOnClickListener(v -> {
                String names = editTextContractorNames.getText().toString().trim();

                prefs.edit()
                        .putString(KEY_CONTRACTOR_NAMES, names)
                        .apply();

                Toast.makeText(this, "Contratante(s) salvo(s)!", Toast.LENGTH_SHORT).show();
            });
        }

        // 🔹 RecyclerView
        dbHelper = new DatabaseHelper(this);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            loadDrinks();
        }

        // 🔹 Botão reorder
        if (btnReorderTasting != null) {
            btnReorderTasting.setOnClickListener(v -> {
                Intent intent = new Intent(DrinkListActivity.this, ReorderTastingActivity.class);
                startActivity(intent);
            });
        }

        // 🔹 Botão adicionar
        if (btnAddNewDrink != null) {
            btnAddNewDrink.setOnClickListener(v -> {
                Intent intent = new Intent(DrinkListActivity.this, AddDrinkActivity.class);
                startActivityForResult(intent, 1);
            });
        }
    }

    private void loadDrinks() {
        drinkList = dbHelper.getAllDrinks();
        adapter = new DrinkAdapter(drinkList, this, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onEditClick(int position) {

        if (position >= 0 && position < drinkList.size()) {
            Drink drink = drinkList.get(position);

            // Abrir tela de edição
            Intent intent = new Intent(DrinkListActivity.this, AddDrinkActivity.class);

            // Passar dados do drink para edição
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("DRINK_ID", drink.getId());
            intent.putExtra("DRINK_NAME", drink.getName());
            intent.putExtra("DRINK_DESCRIPTION", drink.getDescription());
            intent.putExtra("DRINK_SELECTED", drink.isSelected());

            // Verificar se há imagem
            if (drink.getImagePath() != null) {
                intent.putExtra("DRINK_IMAGE_PATH", drink.getImagePath());
            }

            startActivityForResult(intent, REQUEST_EDIT_DRINK);
        } else {
            Toast.makeText(this, "Posição inválida para edição", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteClick(int position) {
        Drink drink = drinkList.get(position);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Excluir Drink")
                .setMessage("Tem certeza que deseja excluir " + drink.getName() + "?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    boolean success = dbHelper.deleteDrinkWithImage(drink.getId());

                    if (success) {
                        loadDrinks(); // Recarregar lista
                        Toast.makeText(this, "Drink excluído com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao excluir drink", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onSelectionChanged(int position, boolean isSelected) {
        Drink drink = drinkList.get(position);
        drink.setSelected(isSelected);
        dbHelper.updateDrinkSelection(drink.getId(), isSelected);

        String message = isSelected ?
                drink.getName() + " selecionado para degustação" :
                drink.getName() + " removido da degustação";

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDrinks();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            loadDrinks();
        }
    }
    public void openMenuSelectionDialog(int drinkId) {

        DatabaseHelper db = new DatabaseHelper(this);

        List<Menu> allMenus = db.getAllMenus();
        List<Integer> selectedMenus = db.getMenuIdsByDrink(drinkId);

        androidx.recyclerview.widget.RecyclerView recyclerView =
                new androidx.recyclerview.widget.RecyclerView(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MenuSelectionAdapter adapter =
                new MenuSelectionAdapter(allMenus, selectedMenus, this);

        recyclerView.setAdapter(adapter);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Associar aos Cardápios")
                .setView(recyclerView)
                .setPositiveButton("Salvar", (dialog, which) -> {

                    // 🔥 remove todas relações antigas
                    for (Integer menuId : selectedMenus) {
                        db.removeDrinkFromMenu(drinkId, menuId);
                    }

                    // 🔥 adiciona as novas
                    for (Integer menuId : adapter.getSelectedIds()) {
                        db.addDrinkToMenu(drinkId, menuId);
                    }

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}