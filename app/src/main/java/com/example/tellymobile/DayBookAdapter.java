package com.example.tellymobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DayBookAdapter extends RecyclerView.Adapter<DayBookAdapter.ViewHolder> {

    private List<DatabaseHelper.VoucherSummary> voucherList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DatabaseHelper.VoucherSummary voucher); // View
        void onEditClick(DatabaseHelper.VoucherSummary voucher);
        void onDeleteClick(DatabaseHelper.VoucherSummary voucher);
    }

    public DayBookAdapter(List<DatabaseHelper.VoucherSummary> list, OnItemClickListener listener) {
        this.voucherList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daybook_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseHelper.VoucherSummary voucher = voucherList.get(position);
        holder.bind(voucher, listener);
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvParty, tvAmount, tvNo;
        android.widget.Button btnEdit, btnDelete; // Use android.widget.Button to be safe or Button

        public ViewHolder(@NonNull View itemView) {
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
            tvAmount.setText("₹" + voucher.amount);
            tvNo.setText("#" + voucher.voucherNo);

            itemView.setOnClickListener(v -> listener.onItemClick(voucher));
            btnEdit.setOnClickListener(v -> listener.onEditClick(voucher));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(voucher));
        }
    }
}
