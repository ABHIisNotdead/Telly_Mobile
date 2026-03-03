package com.example.tellymobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReportDetailAdapter extends RecyclerView.Adapter<ReportDetailAdapter.ViewHolder> {

    public static class ReportDetail {
        public String label;
        public String value;

        public ReportDetail(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }

    private List<ReportDetail> list;

    public ReportDetailAdapter(List<ReportDetail> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportDetail detail = list.get(position);
        holder.tvLabel.setText(detail.label);
        holder.tvValue.setText(detail.value);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvLabel;
        public TextView tvValue;

        public ViewHolder(View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvReportDetailLabel);
            tvValue = itemView.findViewById(R.id.tvReportDetailValue);
        }
    }
}
