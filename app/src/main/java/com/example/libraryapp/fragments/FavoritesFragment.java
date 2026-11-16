package com.example.libraryapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libraryapp.R;
import com.example.libraryapp.activities.BookDetailActivity;
import com.example.libraryapp.adapters.BookAdapter;
import com.example.libraryapp.repository.LibraryRepository;
import com.example.libraryapp.utils.SessionManager;

public class FavoritesFragment extends Fragment {

    private LibraryRepository repository;
    private SessionManager sessionManager;
    private BookAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        repository = new LibraryRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
        tvEmpty = view.findViewById(R.id.tvEmptyFavorites);

        RecyclerView rvFavorites = view.findViewById(R.id.rvFavoriteBooks);
        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookAdapter(book -> {
            Intent intent = new Intent(requireContext(), BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
            startActivity(intent);
        });
        rvFavorites.setAdapter(adapter);

        loadFavorites();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        if (repository != null && sessionManager != null) {
            repository.getFavoriteBooks(sessionManager.getUserId(), books -> {
                if (!isAdded()) return;
                adapter.setBooks(books);
                tvEmpty.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }
    }
}