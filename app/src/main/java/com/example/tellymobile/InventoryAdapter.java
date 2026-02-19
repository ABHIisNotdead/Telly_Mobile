package com.example.tellymobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    public static class InventoryItem {
        int id; // Added ID
        String name;
        double stock;
        double cost;
        double lowStockLimit;

        public InventoryItem(int id, String name, double stock, double cost, double lowStockLimit) {
            this.id = id;
            this.name = name;
            this.stock = stock;
            this.cost = cost;
            this.lowStockLimit = lowStockLimit;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    private final List<InventoryItem> items;
    private final OnItemClickListener listener; // Added Listener

    public InventoryAdapter(List<InventoryItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvStock.setText(String.format(Locale.getDefault(), "%.2f", item.stock));
        holder.tvCost.setText(String.format(Locale.getDefault(), "₹%.2f", item.cost));
        holder.tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", item.stock * item.cost));

        if (item.stock <= item.lowStockLimit && item.lowStockLimit > 0) {
            // Low Stock: Red accents
            holder.tvStock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFEBEE)); // Light Red
            holder.tvStock.setTextColor(0xFFD32F2F); // Dark Red
            holder.cardView.setStrokeColor(0xFFD32F2F);
            holder.cardView.setStrokeWidth(4);
        } else {
            // Normal Stock: Default accent colors
            holder.tvStock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE3F2FD)); // Light Blue
            holder.tvStock.setTextColor(0xFF007AFF); // Azure Blue
            holder.cardView.setStrokeColor(0xFFF8F9FA); // Premium Background color
            holder.cardView.setStrokeWidth(2);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStock, tvCost, tvTotal;
        com.google.android.material.card.MaterialCardView cardView;

        ViewHolder(View view) {
            super(view);
            cardView = (com.google.android.material.card.MaterialCardView) view;
            tvName = view.findViewById(R.id.tvItemName);
            tvStock = view.findViewById(R.id.tvItemStock);
            tvCost = view.findViewById(R.id.tvItemCost);
            tvTotal = view.findViewById(R.id.tvItemTotal);
        }
    }
}
