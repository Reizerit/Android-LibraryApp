package com.example.libraryapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libraryapp.R;
import com.example.libraryapp.activities.AdminActivity;
import com.example.libraryapp.activities.LoginActivity;
import com.example.libraryapp.adapters.BorrowingAdapter;
import com.example.libraryapp.repository.LibraryRepository;
import com.example.libraryapp.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class ProfileFragment extends Fragment {

    private LibraryRepository repository;
    private SessionManager sessionManager;
    private BorrowingAdapter adapter;
    private TextView tvEmptyHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        repository = new LibraryRepository(requireContext());
        sessionManager = new SessionManager(requireContext());

        TextView tvName = view.findViewById(R.id.tvProfileName);
        TextView tvEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvRole = view.findViewById(R.id.tvProfileRole);
        MaterialSwitch switchDarkTheme = view.findViewById(R.id.switchDarkTheme);
        MaterialButton btnAdmin = view.findViewById(R.id.btnOpenAdminPanel);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);

        tvName.setText(sessionManager.getUserName());
        tvEmail.setText(sessionManager.getUserEmail());
        if (sessionManager.isAdmin()) {
            tvRole.setText(R.string.profile_role_admin);
            btnAdmin.setVisibility(View.VISIBLE);
            btnAdmin.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), AdminActivity.class)));
        } else {
            tvRole.setText(R.string.profile_role_user);
            btnAdmin.setVisibility(View.GONE);
        }

        switchDarkTheme.setChecked(sessionManager.isDarkTheme());
        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setDarkTheme(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        RecyclerView rvBorrowings = view.findViewById(R.id.rvUserBorrowings);
        rvBorrowings.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BorrowingAdapter(false, borrowing ->
                repository.returnBook(borrowing.getId(), borrowing.getBookId(), success -> {
                    if (!isAdded()) return;
                    if (success) {
                        Toast.makeText(requireContext(), R.string.msg_book_returned, Toast.LENGTH_SHORT).show();
                        loadHistory();
                    }
                }));
        rvBorrowings.setAdapter(adapter);

        loadHistory();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        if (repository != null && sessionManager != null) {
            repository.getUserBorrowings(sessionManager.getUserId(), list -> {
                if (!isAdded()) return;
                adapter.setBorrowings(list);
                tvEmptyHistory.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }
    }
}