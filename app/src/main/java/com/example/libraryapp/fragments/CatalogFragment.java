package com.example.libraryapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libraryapp.R;
import com.example.libraryapp.activities.BookDetailActivity;
import com.example.libraryapp.adapters.BookAdapter;
import com.example.libraryapp.models.Category;
import com.example.libraryapp.repository.LibraryRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CatalogFragment extends Fragment {

    private LibraryRepository repository;
    private BookAdapter adapter;
    private Spinner spinnerCategories;
    private TextView tvEmpty;
    private String currentQuery = "";
    private int selectedCategoryId = 0;
    private final List<Category> categoryList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        repository = new LibraryRepository(requireContext());
        TextInputEditText etSearch = view.findViewById(R.id.etSearchCatalog);
        spinnerCategories = view.findViewById(R.id.spinnerCategories);
        tvEmpty = view.findViewById(R.id.tvEmptyCatalog);

        RecyclerView rvCatalog = view.findViewById(R.id.rvCatalogBooks);
        rvCatalog.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BookAdapter(book -> {
            Intent intent = new Intent(requireContext(), BookDetailActivity.class);
            intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.getId());
            startActivity(intent);
        });
        rvCatalog.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s != null ? s.toString() : "";
                filterBooks();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadCategories();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategories();
    }

    private void loadCategories() {
        repository.getAllCategories(categories -> {
            if (!isAdded()) return;
            categoryList.clear();
            categoryList.add(new Category(0, getString(R.string.all_categories)));
            categoryList.addAll(categories);

            ArrayAdapter<Category> spinnerAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categoryList
            );
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategories.setAdapter(spinnerAdapter);

            spinnerCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedCategoryId = categoryList.get(position).getId();
                    filterBooks();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
            filterBooks();
        });
    }

    private void filterBooks() {
        repository.getBooks(currentQuery, selectedCategoryId, books -> {
            if (!isAdded()) return;
            adapter.setBooks(books);
            tvEmpty.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}