package com.will.bartendermenu.ui.menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.will.bartendermenu.R;
import com.will.bartendermenu.model.Drink;

import java.util.List;

public class MenuDrinkAdapter extends RecyclerView.Adapter<MenuDrinkAdapter.ViewHolder> {

    private List<Drink> list;

    public MenuDrinkAdapter(List<Drink> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reorder_drink, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Drink drink = list.get(position);

        holder.tvDrinkName.setText(drink.getName());
        holder.tvPosition.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDrinkName;
        TextView tvPosition;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDrinkName = itemView.findViewById(R.id.tvDrinkName);
            tvPosition = itemView.findViewById(R.id.tvPosition);
        }
    }
}