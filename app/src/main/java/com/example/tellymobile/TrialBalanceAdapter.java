package com.example.tellymobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class TrialBalanceAdapter extends RecyclerView.Adapter<TrialBalanceAdapter.ViewHolder> {

    private List<DatabaseHelper.TrialBalanceRow> rowList;

    public TrialBalanceAdapter(List<DatabaseHelper.TrialBalanceRow> rowList) {
        this.rowList = rowList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trial_balance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DatabaseHelper.TrialBalanceRow row = rowList.get(position);
        holder.tvLedgerName.setText(row.ledgerName);
        holder.tvGroupName.setText(row.groupName);
        
        if (row.debit > 0) {
            holder.tvDebit.setText(String.format(Locale.getDefault(), "%.2f", row.debit));
        } else {
            holder.tvDebit.setText("");
        }
        
        if (row.credit > 0) {
            holder.tvCredit.setText(String.format(Locale.getDefault(), "%.2f", row.credit));
        } else {
            holder.tvCredit.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return rowList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLedgerName, tvGroupName, tvDebit, tvCredit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLedgerName = itemView.findViewById(R.id.tvLedgerName);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvDebit = itemView.findViewById(R.id.tvDebit);
            tvCredit = itemView.findViewById(R.id.tvCredit);
        }
    }
}
