package com.example.tellymobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tellymobile.DatabaseHelper.Company;

import java.util.List;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder> {

    private List<Company> companyList;
    private OnCompanyClickListener listener;

    public interface OnCompanyClickListener {
        void onCompanyClick(Company company);
    }

    public CompanyAdapter(List<Company> companyList, OnCompanyClickListener listener) {
        this.companyList = companyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_company_card, parent, false);
        return new CompanyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyViewHolder holder, int position) {
        Company company = companyList.get(position);
        
        holder.tvName.setText(company.name);
        holder.tvAddress.setText(company.address != null && !company.address.isEmpty() ? company.address : "No Address");
        
        if (company.gst != null && !company.gst.isEmpty()) {
            holder.tvGst.setText("GST: " + company.gst);
            holder.tvGst.setVisibility(View.VISIBLE);
        } else {
            holder.tvGst.setVisibility(View.GONE);
        }

        // Logic for logo loading if URI stored in future, currently placeholder
        // holder.ivLogo.setImageURI(Uri.parse(company.logoUri));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCompanyClick(company);
            }
        });
    }

    @Override
    public int getItemCount() {
        return companyList.size();
    }

    public static class CompanyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvGst;
        ImageView ivLogo;

        public CompanyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCompanyName);
            tvAddress = itemView.findViewById(R.id.tvCompanyAddress);
            tvGst = itemView.findViewById(R.id.tvCompanyGst);
            ivLogo = itemView.findViewById(R.id.ivCompanyLogo);
        }
    }
}
