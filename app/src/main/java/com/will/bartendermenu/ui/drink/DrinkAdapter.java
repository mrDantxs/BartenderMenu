package com.will.bartendermenu.ui.drink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.will.bartendermenu.model.Drink;
import com.will.bartendermenu.R;

import java.util.List;

public class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder> {

    private List<Drink> drinkList;
    private OnDrinkClickListener listener;

    public interface OnDrinkClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onSelectionChanged(int position, boolean isSelected);
    }

    public DrinkAdapter(List<Drink> drinkList, OnDrinkClickListener listener) {
        this.drinkList = drinkList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drink, parent, false);
        return new DrinkViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        Drink drink = drinkList.get(position);
        holder.bind(drink, position);
    }

    @Override
    public int getItemCount() {
        return drinkList.size();
    }

    // Classe interna DrinkViewHolder
    public static class DrinkViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBoxSelected;
        private TextView textViewDrinkName;
        private TextView textViewDrinkDescription;
        private TextView textViewStatus;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        private OnDrinkClickListener listener;

        public DrinkViewHolder(@NonNull View itemView, OnDrinkClickListener listener) {
            super(itemView);
            this.listener = listener;

            // Inicializar views
            checkBoxSelected = itemView.findViewById(R.id.checkBoxSelected);
            textViewDrinkName = itemView.findViewById(R.id.textViewDrinkName);
            textViewDrinkDescription = itemView.findViewById(R.id.textViewDrinkDescription);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Drink drink, int position) {
            // Configurar textos
            textViewDrinkName.setText(drink.getName());
            textViewDrinkDescription.setText(drink.getDescription());

            // Configurar checkbox (sem disparar listener durante bind)
            checkBoxSelected.setOnCheckedChangeListener(null);
            checkBoxSelected.setChecked(drink.isSelected());

            // Configurar status
            if (drink.isSelected()) {
                textViewStatus.setText("SELECIONADO");
                textViewStatus.setBackgroundColor(0xFF4CAF50); // Verde
            } else {
                textViewStatus.setText("NÃO SELECIONADO");
                textViewStatus.setBackgroundColor(0xFFF44336); // Vermelho
            }

            // Configurar listener do botão EDITAR
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(position);
                }
            });

            // Configurar listener do botão EXCLUIR
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(position);
                }
            });

            // Configurar listener do checkbox
            checkBoxSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onSelectionChanged(position, isChecked);
                }
            });
        }
    }
}