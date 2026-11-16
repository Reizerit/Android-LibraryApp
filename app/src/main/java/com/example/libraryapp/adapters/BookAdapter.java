package com.example.libraryapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libraryapp.R;
import com.example.libraryapp.models.Book;
import com.example.libraryapp.utils.CoverHelper;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    private List<Book> books = new ArrayList<>();
    private final OnBookClickListener listener;

    public BookAdapter(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<Book> newBooks) {
        this.books = newBooks != null ? newBooks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        Context context = holder.itemView.getContext();

        holder.tvCategory.setText(book.getCategoryName());
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvDescription.setText(book.getDescription());

        holder.viewCover.setBackgroundResource(CoverHelper.getCoverDrawable(book.getCoverResId()));
        String firstLetter = (book.getTitle() != null && !book.getTitle().isEmpty())
                ? book.getTitle().substring(0, 1).toUpperCase()
                : "К";
        holder.tvCoverLetter.setText(firstLetter);

        if (book.getAvailableCopies() > 0) {
            holder.tvAvailability.setText(context.getString(R.string.status_available_text, book.getAvailableCopies()));
            holder.tvAvailability.setTextColor(ContextCompat.getColor(context, R.color.status_available));
        } else {
            holder.tvAvailability.setText(R.string.status_unavailable_text);
            holder.tvAvailability.setTextColor(ContextCompat.getColor(context, R.color.status_unavailable));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        FrameLayout viewCover;
        TextView tvCoverLetter, tvCategory, tvTitle, tvAuthor, tvDescription, tvAvailability;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCover = itemView.findViewById(R.id.viewBookCover);
            tvCoverLetter = itemView.findViewById(R.id.tvCoverLetter);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvAuthor = itemView.findViewById(R.id.tvItemAuthor);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvAvailability = itemView.findViewById(R.id.tvItemAvailability);
        }
    }
}