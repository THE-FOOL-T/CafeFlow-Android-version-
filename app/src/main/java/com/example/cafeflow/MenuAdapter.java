package com.example.cafeflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<MenuItem> menuItems;
    private OnAdminControlsListener adminListener;
    private boolean isAdminMode;

    public interface OnAdminControlsListener {
        void onIncreaseStockClick(MenuItem item);
        void onDecreaseStockClick(MenuItem item);
        void onRemoveItemClick(MenuItem item);
        void onAddToCartClick(MenuItem item);
    }

    public MenuAdapter(List<MenuItem> menuItems, OnAdminControlsListener listener, boolean isAdminMode) {
        this.menuItems = menuItems;
        this.adminListener = listener;
        this.isAdminMode = isAdminMode;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_grid_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        holder.itemName.setText(item.getName());
        holder.itemPrice.setText(String.format("$%.2f", item.getPrice()));
        holder.itemStock.setText(String.format("Stock: %d", item.getStockQuantity()));

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get().load(item.getImageUrl()).into(holder.itemImage);
        }

        if (isAdminMode) {
            holder.adminControls.setVisibility(View.VISIBLE);
            holder.addToCartButton.setVisibility(View.GONE);

            holder.increaseStockButton.setOnClickListener(v -> adminListener.onIncreaseStockClick(item));
            holder.decreaseStockButton.setOnClickListener(v -> adminListener.onDecreaseStockClick(item));
            holder.removeItemButton.setOnClickListener(v -> adminListener.onRemoveItemClick(item));
        } else {
            holder.adminControls.setVisibility(View.GONE);
            holder.addToCartButton.setVisibility(View.VISIBLE);
            holder.addToCartButton.setOnClickListener(v -> adminListener.onAddToCartClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemPrice, itemStock;
        Button addToCartButton;
        ImageButton increaseStockButton, decreaseStockButton, removeItemButton;
        LinearLayout adminControls;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImageView);
            itemName = itemView.findViewById(R.id.itemNameTextView);
            itemPrice = itemView.findViewById(R.id.itemPriceTextView);
            itemStock = itemView.findViewById(R.id.itemStockTextView);
            addToCartButton = itemView.findViewById(R.id.addToOrderButton);
            adminControls = itemView.findViewById(R.id.admin_controls);
            increaseStockButton = itemView.findViewById(R.id.increaseStockButton);
            decreaseStockButton = itemView.findViewById(R.id.decreaseStockButton);
            removeItemButton = itemView.findViewById(R.id.removeItemButton);
        }
    }
}