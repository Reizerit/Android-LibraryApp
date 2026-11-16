package com.example.libraryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libraryapp.R;
import com.example.libraryapp.models.Borrowing;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class BorrowingAdapter extends RecyclerView.Adapter<BorrowingAdapter.BorrowingViewHolder> {

    public interface OnReturnClickListener {
        void onReturnClick(Borrowing borrowing);
    }

    private List<Borrowing> list = new ArrayList<>();
    private final boolean isAdminMode;
    private final OnReturnClickListener returnListener;

    public BorrowingAdapter(boolean isAdminMode, OnReturnClickListener returnListener) {
        this.isAdminMode = isAdminMode;
        this.returnListener = returnListener;
    }

    public void setBorrowings(List<Borrowing> newList) {
        this.list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BorrowingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_borrowing, parent, false);
        return new BorrowingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowingViewHolder holder, int position) {
        Borrowing item = list.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(item.getBookTitle());
        holder.tvAuthor.setText(item.getBookAuthor());

        if (isAdminMode) {
            holder.tvUserName.setVisibility(View.VISIBLE);
            holder.tvUserName.setText("Читатель: " + item.getUserName());
        } else {
            holder.tvUserName.setVisibility(View.GONE);
        }

        if (item.isBorrowed()) {
            holder.tvStatus.setText(context.getString(R.string.status_on_hands, item.getBorrowDate()));
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent));

            if (!isAdminMode && returnListener != null) {
                holder.btnReturn.setVisibility(View.VISIBLE);
                holder.btnReturn.setOnClickListener(v -> returnListener.onReturnClick(item));
            } else {
                holder.btnReturn.setVisibility(View.GONE);
            }
        } else {
            String retDate = item.getReturnDate() != null ? item.getReturnDate() : "";
            holder.tvStatus.setText(context.getString(R.string.status_returned, retDate));
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_available));
            holder.btnReturn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class BorrowingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvUserName, tvStatus;
        MaterialButton btnReturn;

        BorrowingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBorrowBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBorrowBookAuthor);
            tvUserName = itemView.findViewById(R.id.tvBorrowUserName);
            tvStatus = itemView.findViewById(R.id.tvBorrowStatus);
            btnReturn = itemView.findViewById(R.id.btnReturnBook);
        }
    }
}