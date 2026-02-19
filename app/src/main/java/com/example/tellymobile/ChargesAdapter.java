package com.example.tellymobile;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChargesAdapter extends RecyclerView.Adapter<ChargesAdapter.ChargeViewHolder> {
    private List<VoucherCharge> list;
    private OnRemoveListener listener;
    
    public interface OnRemoveListener {
        void onRemove(int position);
    }
    
    public ChargesAdapter(List<VoucherCharge> list, OnRemoveListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @androidx.annotation.NonNull
    @Override
    public ChargeViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_charge_row, parent, false);
        return new ChargeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull ChargeViewHolder holder, int position) {
        VoucherCharge charge = list.get(position);
        holder.tvName.setText(charge.ledgerName);
        if (charge.isPercentage) {
            holder.tvValue.setText("@ " + charge.rate + "%");
        } else {
            holder.tvValue.setText("(Fixed)");
        }
        holder.tvAmount.setText(String.format("₹%.2f", charge.amount));
        holder.btnRemove.setOnClickListener(v -> listener.onRemove(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    
    public static class ChargeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvValue, tvAmount;
        android.widget.ImageButton btnRemove;
        
        public ChargeViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChargeName);
            tvValue = itemView.findViewById(R.id.tvChargeValue);
            tvAmount = itemView.findViewById(R.id.tvChargeAmount);
            btnRemove = itemView.findViewById(R.id.btnRemoveCharge);
        }
    }
}
