package com.will.bartendermenu.ui.menu;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.will.bartendermenu.R;
import com.will.bartendermenu.database.DatabaseHelper;
import com.will.bartendermenu.model.Menu;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {
    public interface OnMenuClickListener {
        void onMenuClick(Menu menu);
    }

    private List<Menu> list;
    private Context context;
    private DatabaseHelper db;
    private OnMenuClickListener listener;

    public MenuAdapter(List<Menu> list, Context context, DatabaseHelper db, OnMenuClickListener listener) {
        this.list = list;
        this.context = context;
        this.db = db;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        Menu menu = list.get(position);

        holder.txtName.setText(menu.getName());
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onMenuClick(list.get(pos));
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Excluir")
                    .setMessage("Deseja excluir este cardápio?")
                    .setPositiveButton("Sim", (d, w) -> {
                        db.deleteMenu(menu.getId());
                        list.remove(position);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("Não", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        ImageButton btnDelete;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtMenuName);
            btnDelete = itemView.findViewById(R.id.btnDeleteMenu);
        }
    }
}