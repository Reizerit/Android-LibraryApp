package com.example.libraryapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libraryapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminItemAdapter extends RecyclerView.Adapter<AdminItemAdapter.AdminViewHolder> {

    public static class AdminRow {
        public final int id;
        public final String title;
        public final String subtitle;
        public final boolean showActions;

        public AdminRow(int id, String title, String subtitle, boolean showActions) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.showActions = showActions;
        }
    }

    public interface OnAdminActionListener {
        void onEdit(int id);
        void onDelete(int id);
    }

    private List<AdminRow> rows = new ArrayList<>();
    private final OnAdminActionListener listener;

    public AdminItemAdapter(OnAdminActionListener listener) {
        this.listener = listener;
    }

    public void setRows(List<AdminRow> newRows) {
        this.rows = newRows != null ? newRows : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_simple, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        AdminRow row = rows.get(position);
        holder.tvTitle.setText(row.title);
        holder.tvSubtitle.setText(row.subtitle);

        if (row.showActions && listener != null) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(row.id));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(row.id));
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        MaterialButton btnEdit, btnDelete;

        AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAdminTitle);
            tvSubtitle = itemView.findViewById(R.id.tvAdminSubtitle);
            btnEdit = itemView.findViewById(R.id.btnAdminEdit);
            btnDelete = itemView.findViewById(R.id.btnAdminDelete);
        }
    }
}