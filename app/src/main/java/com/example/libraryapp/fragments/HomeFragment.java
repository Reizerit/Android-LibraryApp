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

public class HomeFragment extends Fragment {

    private LibraryRepository repository;
    private BookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        repository = new LibraryRepository(requireContext());
        SessionManager sessionManager = new SessionManager(requireContext());

        TextView tvGreeting = view.findViewById(R.id.tvHomeUserGreeting);
        tvGreeting.setText("Здравствуйте, " + sessionManager.getUserName() + "!");

        RecyclerView rvHome = view.findViewById(R.id.rvHomeBooks);
        rvHome.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookAdapter(book -> {
            Intent intent = new Intent(requireContext(), BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
            startActivity(intent);
        });
        rvHome.setAdapter(adapter);

        loadBooks();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBooks();
    }

    private void loadBooks() {
        if (repository != null) {
            repository.getBooks("", 0, books -> {
                if (isAdded()) {
                    adapter.setBooks(books);
                }
            });
        }
    }
}