package com.example.tellymobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.ViewHolder> {

    private List<InvoiceItem> invoiceItems;

    public interface OnDeleteListener {
        void onDelete(int position);
    }
    
    private OnDeleteListener deleteListener;
    
    public InvoiceAdapter(List<InvoiceItem> invoiceItems, OnDeleteListener deleteListener) {
        this.invoiceItems = invoiceItems;
        this.deleteListener = deleteListener;
    }

    public InvoiceAdapter(List<InvoiceItem> invoiceItems) {
        this.invoiceItems = invoiceItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invoice_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvoiceItem item = invoiceItems.get(position);
        holder.tvItemName.setText(item.getItemName());
        holder.tvQty.setText(String.valueOf(item.getQuantity()));
        holder.tvRate.setText(String.valueOf(item.getRate()));
        holder.tvAmount.setText(String.valueOf(item.getAmount()));
        
        if (holder.btnDelete != null && deleteListener != null) {
            holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(position));
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else if (holder.btnDelete != null) {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return invoiceItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvQty, tvRate, tvAmount;
        android.widget.ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvRate = itemView.findViewById(R.id.tvRate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
