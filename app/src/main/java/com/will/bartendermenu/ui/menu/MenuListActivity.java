package com.will.bartendermenu.ui.menu;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.will.bartendermenu.R;
import com.will.bartendermenu.database.DatabaseHelper;
import com.will.bartendermenu.model.Menu;

import java.util.List;

public class MenuListActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private MenuAdapter adapter;
    private RecyclerView recyclerMenus;
    private EditText edtMenuName;
    private Button btnAddMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);

        db = new DatabaseHelper(this);

        recyclerMenus = findViewById(R.id.recyclerViewMenus);
        edtMenuName = findViewById(R.id.edtMenuName);
        btnAddMenu = findViewById(R.id.btnAddMenu);

        recyclerMenus.setLayoutManager(new LinearLayoutManager(this));

        loadMenus();

        btnAddMenu.setOnClickListener(v -> addMenu());
    }

    private void loadMenus() {
        List<Menu> list = db.getAllMenus();

        adapter = new MenuAdapter(list, this, db, menu -> {
            android.util.Log.d("MENU_DEBUG", "clicou menu id=" + menu.getId());

            Intent intent = new Intent(MenuListActivity.this, MenuDrinksActivity.class);
            intent.putExtra("menu_id", menu.getId());
            startActivity(intent);
        });

        recyclerMenus.setAdapter(adapter);
    }

    private void addMenu() {
        String name = edtMenuName.getText().toString().trim();

        if (name.isEmpty()) {
            edtMenuName.setError("Informe o nome");
            return;
        }

        db.addMenu(name);
        edtMenuName.setText("");
        loadMenus();
    }
}