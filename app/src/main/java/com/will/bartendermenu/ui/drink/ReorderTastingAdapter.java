package com.will.bartendermenu.ui.drink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.will.bartendermenu.R;
import com.will.bartendermenu.model.Drink;

import java.util.Collections;
import java.util.List;

public class ReorderTastingAdapter extends RecyclerView.Adapter<ReorderTastingAdapter.ViewHolder> {

    private List<Drink> drinks;
    private OnStartDragListener dragListener;

    public interface OnStartDragListener {
        void onStartDrag(ViewHolder viewHolder);
    }

    public ReorderTastingAdapter(List<Drink> drinks, OnStartDragListener listener) {
        this.drinks = drinks;
        this.dragListener = listener;
    }

    public List<Drink> getDrinks() {
        return drinks;
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
        Drink drink = drinks.get(position);
        holder.tvDrinkName.setText(drink.getName());
        holder.tvPosition.setText(String.valueOf(position + 1));

        holder.ivDragHandle.setOnTouchListener((v, event) -> {
            dragListener.onStartDrag(holder);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return drinks.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(drinks, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        notifyItemRangeChanged(0, drinks.size());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDrinkName, tvPosition;
        ImageView ivDragHandle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDrinkName = itemView.findViewById(R.id.tvDrinkName);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            ivDragHandle = itemView.findViewById(R.id.ivDragHandle);
        }
    }
}
