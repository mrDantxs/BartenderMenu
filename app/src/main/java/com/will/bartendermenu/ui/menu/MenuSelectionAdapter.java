package com.will.bartendermenu.ui.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.will.bartendermenu.R;
import com.will.bartendermenu.model.Menu;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MenuSelectionAdapter extends RecyclerView.Adapter<MenuSelectionAdapter.ViewHolder> {

    private List<Menu> menus;
    private Set<Integer> selectedIds;
    private Context context;

    public MenuSelectionAdapter(List<Menu> menus, List<Integer> alreadySelected, Context context) {
        this.menus = menus;
        this.context = context;
        this.selectedIds = new HashSet<>(alreadySelected);
    }

    public Set<Integer> getSelectedIds() {
        return selectedIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_menu_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Menu menu = menus.get(position);

        holder.checkBox.setText(menu.getName());
        holder.checkBox.setChecked(selectedIds.contains(menu.getId()));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedIds.add(menu.getId());
            } else {
                selectedIds.remove(menu.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkMenu);
        }
    }
}