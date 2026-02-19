package com.example.tellymobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class LedgerTransactionAdapter extends RecyclerView.Adapter<LedgerTransactionAdapter.ViewHolder> {

    private List<DatabaseHelper.LedgerTransaction> transactionList;

    public LedgerTransactionAdapter(List<DatabaseHelper.LedgerTransaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ledger_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseHelper.LedgerTransaction transaction = transactionList.get(position);
        holder.tvDate.setText(transaction.date);
        
        String info = transaction.type + " " + (transaction.voucherNo != null ? "#" + transaction.voucherNo : "");
        holder.tvVoucherInfo.setText(info);
        
        holder.tvNarration.setText(transaction.narration != null ? transaction.narration : "");
        
        if (transaction.debit > 0) {
            holder.tvDebit.setText(String.format(Locale.getDefault(), "%.2f", transaction.debit));
        } else {
            holder.tvDebit.setText("");
        }
        
        if (transaction.credit > 0) {
            holder.tvCredit.setText(String.format(Locale.getDefault(), "%.2f", transaction.credit));
        } else {
            holder.tvCredit.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvVoucherInfo, tvNarration, tvDebit, tvCredit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvVoucherInfo = itemView.findViewById(R.id.tvVoucherInfo);
            tvNarration = itemView.findViewById(R.id.tvNarration);
            tvDebit = itemView.findViewById(R.id.tvDebit);
            tvCredit = itemView.findViewById(R.id.tvCredit);
        }
    }
}
