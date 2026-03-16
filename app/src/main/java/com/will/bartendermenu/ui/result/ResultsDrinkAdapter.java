package com.will.bartendermenu.ui.result;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.will.bartendermenu.model.Drink;
import android.content.Context;
import com.will.bartendermenu.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
public class ResultsDrinkAdapter extends RecyclerView.Adapter<ResultsDrinkAdapter.ViewHolder> {

    private List<Drink> drinks;
    private Context context;

    public ResultsDrinkAdapter(Context context, List<Drink> drinks) {
        this.context = context;
        this.drinks = drinks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_result_drink, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Drink drink = drinks.get(position);

        if (drink == null) {
            holder.itemView.setVisibility(View.INVISIBLE);
            return;
        }

        holder.itemView.setVisibility(View.VISIBLE);

        holder.name.setText(drink.getName());

        String imagePath = drink.getImagePath();

        if (imagePath != null && !imagePath.isEmpty()) {

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            if (bitmap != null) {
                holder.image.setImageBitmap(bitmap);
            } else {
                holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
            }

        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return drinks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.imageDrink);
            name = itemView.findViewById(R.id.textDrinkName);
        }
    }
}
