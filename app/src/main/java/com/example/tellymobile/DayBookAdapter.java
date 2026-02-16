package com.example.tellymobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DayBookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Object> items;
    private OnItemClickListener listener;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnItemClickListener {
        void onItemClick(DatabaseHelper.VoucherSummary voucher);
        void onEditClick(DatabaseHelper.VoucherSummary voucher);
        void onDeleteClick(DatabaseHelper.VoucherSummary voucher);
    }

    public DayBookAdapter(List<Object> list, OnItemClickListener listener) {
        this.items = list;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_daybook_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_daybook_row, parent, false);
            return new VoucherViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            ((HeaderViewHolder) holder).tvHeader.setText((String) items.get(position));
        } else {
            ((VoucherViewHolder) holder).bind((DatabaseHelper.VoucherSummary) items.get(position), listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<Object> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvDateHeader);
        }
    }

    public static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvParty, tvAmount, tvNo;
        android.widget.Button btnEdit, btnDelete;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvVoucherType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvParty = itemView.findViewById(R.id.tvPartyName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvNo = itemView.findViewById(R.id.tvVoucherNo);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(final DatabaseHelper.VoucherSummary voucher, final OnItemClickListener listener) {
            tvType.setText(voucher.type);
            tvDate.setText(voucher.date);
            tvParty.setText(voucher.partyName);
            tvAmount.setText("₹" + String.format("%.2f", voucher.amount));
            tvNo.setText("#" + voucher.voucherNo);

            // Type-based styling
            int color = 0xFF3F51B5; // Default primary
            if (voucher.type.equalsIgnoreCase("Sales") || voucher.type.equalsIgnoreCase("Receipt")) {
                color = 0xFF4CAF50; // Green for inflow
            } else if (voucher.type.equalsIgnoreCase("Purchase") || voucher.type.equalsIgnoreCase("Payment")) {
                color = 0xFFF44336; // Red for outflow
            }
            tvType.setTextColor(color);

            itemView.setOnClickListener(v -> listener.onItemClick(voucher));
            btnEdit.setOnClickListener(v -> listener.onEditClick(voucher));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(voucher));
        }
    }
}
