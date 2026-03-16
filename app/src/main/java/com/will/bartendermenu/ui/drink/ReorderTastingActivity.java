package com.will.bartendermenu.ui.drink;

import android.os.Bundle;
import android.widget.Toast;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.will.bartendermenu.R;
import com.will.bartendermenu.database.DatabaseHelper;
import com.will.bartendermenu.model.Drink;

import java.util.List;

public class ReorderTastingActivity extends AppCompatActivity
        implements ReorderTastingAdapter.OnStartDragListener {

    private RecyclerView recyclerView;
    private ReorderTastingAdapter adapter;
    private DatabaseHelper dbHelper;
    private ItemTouchHelper touchHelper;
    private Button btnSaveOrder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reorder_tasting);

        recyclerView = findViewById(R.id.recyclerViewReorder);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnSaveOrder = findViewById(R.id.btnSaveOrder);

        dbHelper = new DatabaseHelper(this);

        btnSaveOrder.setOnClickListener(v -> saveTastingOrder());
        loadSelectedDrinks();
    }

    private void loadSelectedDrinks() {
        List<Drink> selectedDrinks = dbHelper.getSelectedDrinksForTasting();

        if (selectedDrinks == null || selectedDrinks.isEmpty()) {
            Toast.makeText(this, "Nenhum drink selecionado para degustação", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter = new ReorderTastingAdapter(selectedDrinks, this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                0) {

            @Override
            public boolean onMove(RecyclerView rv,
                                  RecyclerView.ViewHolder vh,
                                  RecyclerView.ViewHolder target) {

                int from = vh.getAdapterPosition();
                int to = target.getAdapterPosition();
                adapter.onItemMove(from, to);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int direction) {
                // Não vamos usar swipe
            }
        };

        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onStartDrag(ReorderTastingAdapter.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void saveTastingOrder() {
        if (adapter == null) {
            Toast.makeText(this, "Nada para salvar", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Drink> orderedDrinks = adapter.getDrinks();

        if (orderedDrinks.isEmpty()) {
            Toast.makeText(this, "Nenhum drink selecionado", Toast.LENGTH_SHORT).show();
            return;
        }

        //  Limpar ordem antiga
        dbHelper.clearTastingOrder();

        // Salvar nova ordem
        for (int i = 0; i < orderedDrinks.size(); i++) {
            Drink d = orderedDrinks.get(i);
            dbHelper.updateTastingOrder(d.getId(), i + 1);
        }

        Toast.makeText(this, "Ordem de degustação salva!", Toast.LENGTH_SHORT).show();
        finish();
    }

}
