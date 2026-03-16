package com.will.bartendermenu.ui.menu;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.will.bartendermenu.R;
import com.will.bartendermenu.database.DatabaseHelper;
import com.will.bartendermenu.model.Drink;

import java.util.Collections;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.content.Intent;
import android.widget.Button;
import com.will.bartendermenu.ui.tasting.TastingActivity;

import java.util.List;

public class MenuDrinksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper db;
    private int menuId;
    private List<Drink> drinks;
    private MenuDrinkAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_drinks);

        // ✅ pegar menu_id da intent
        menuId = getIntent().getIntExtra("menu_id", -1);
        Button btnStart = findViewById(R.id.btnStartTasting);

        btnStart.setOnClickListener(v -> {

            Intent intent = new Intent(this, TastingActivity.class);
            intent.putExtra("menu_id", menuId);
            startActivity(intent);

        });

        Log.d("MENU_DEBUG", "menuId = " + menuId);

        // ✅ init banco
        db = new DatabaseHelper(this);

        // ✅ recycler
        recyclerView = findViewById(R.id.recyclerViewMenuDrinks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ carregar drinks
        loadDrinks();
    }

    private void loadDrinks() {

        drinks = db.getDrinksByMenu(menuId);

        Log.d("MENU_DEBUG", "qtd drinks = " + drinks.size());

        adapter = new MenuDrinkAdapter(drinks);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        0) {

                    @Override
                    public boolean onMove(
                            RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder,
                            RecyclerView.ViewHolder target) {

                        int from = viewHolder.getAdapterPosition();
                        int to = target.getAdapterPosition();

                        Collections.swap(drinks, from, to);
                        adapter.notifyItemMoved(from, to);

                        saveDrinkOrder();

                        return true;
                    }

                    @Override
                    public void onSwiped(
                            RecyclerView.ViewHolder viewHolder,
                            int direction) {
                    }
                };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }
    private void saveDrinkOrder() {

        for (int i = 0; i < drinks.size(); i++) {

            Drink d = drinks.get(i);

            db.updateMenuDrinkOrder(
                    menuId,
                    d.getId(),
                    i
            );
        }

    }
}