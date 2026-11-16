package com.example.libraryapp.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libraryapp.R;
import com.example.libraryapp.adapters.AdminItemAdapter;
import com.example.libraryapp.adapters.BorrowingAdapter;
import com.example.libraryapp.models.Book;
import com.example.libraryapp.models.Category;
import com.example.libraryapp.models.User;
import com.example.libraryapp.repository.LibraryRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private LibraryRepository repository;
    private RecyclerView rvAdmin;
    private MaterialButton btnAdd;
    private int currentTab = 0; // 0 = Книги, 1 = Категории, 2 = Читатели, 3 = Выдачи

    private List<Book> cachedBooks = new ArrayList<>();
    private List<Category> cachedCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        repository = new LibraryRepository(this);
        MaterialButton btnBack = findViewById(R.id.btnAdminBack);
        TabLayout tabLayout = findViewById(R.id.tabLayoutAdmin);
        btnAdd = findViewById(R.id.btnAdminAdd);
        rvAdmin = findViewById(R.id.rvAdminList);
        rvAdmin.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v -> {
            if (currentTab == 0) {
                showBookDialog(null);
            } else if (currentTab == 1) {
                showCategoryDialog(null);
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                refreshCurrentTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        refreshCurrentTab();
    }

    private void refreshCurrentTab() {
        if (currentTab == 0) {
            btnAdd.setVisibility(View.VISIBLE);
            btnAdd.setText(R.string.btn_add_book);
            loadBooksTab();
        } else if (currentTab == 1) {
            btnAdd.setVisibility(View.VISIBLE);
            btnAdd.setText(R.string.btn_add_category);
            loadCategoriesTab();
        } else if (currentTab == 2) {
            btnAdd.setVisibility(View.GONE);
            loadUsersTab();
        } else {
            btnAdd.setVisibility(View.GONE);
            loadBorrowingsTab();
        }
    }

    private void loadBooksTab() {
        repository.getAllCategories(categories -> cachedCategories = categories);
        repository.getBooks("", 0, books -> {
            cachedBooks = books;
            AdminItemAdapter adapter = new AdminItemAdapter(new AdminItemAdapter.OnAdminActionListener() {
                @Override
                public void onEdit(int id) {
                    for (Book b : cachedBooks) {
                        if (b.getId() == id) {
                            showBookDialog(b);
                            break;
                        }
                    }
                }

                @Override
                public void onDelete(int id) {
                    repository.deleteBook(id, ok -> refreshCurrentTab());
                }
            });
            List<AdminItemAdapter.AdminRow> rows = new ArrayList<>();
            for (Book b : books) {
                rows.add(new AdminItemAdapter.AdminRow(
                        b.getId(),
                        b.getTitle(),
                        b.getAuthor() + " • " + b.getCategoryName() +
                                " (доступно: " + b.getAvailableCopies() + "/" + b.getTotalCopies() + ")",
                        true
                ));
            }
            adapter.setRows(rows);
            rvAdmin.setAdapter(adapter);
        });
    }

    private void loadCategoriesTab() {
        repository.getAllCategories(categories -> {
            cachedCategories = categories;
            AdminItemAdapter adapter = new AdminItemAdapter(new AdminItemAdapter.OnAdminActionListener() {
                @Override
                public void onEdit(int id) {
                    for (Category c : cachedCategories) {
                        if (c.getId() == id) {
                            showCategoryDialog(c);
                            break;
                        }
                    }
                }

                @Override
                public void onDelete(int id) {
                    repository.deleteCategory(id, ok -> refreshCurrentTab());
                }
            });
            List<AdminItemAdapter.AdminRow> rows = new ArrayList<>();
            for (Category c : categories) {
                rows.add(new AdminItemAdapter.AdminRow(c.getId(), c.getName(), "Категория книг", true));
            }
            adapter.setRows(rows);
            rvAdmin.setAdapter(adapter);
        });
    }

    private void loadUsersTab() {
        repository.getAllUsers(users -> {
            AdminItemAdapter adapter = new AdminItemAdapter(null);
            List<AdminItemAdapter.AdminRow> rows = new ArrayList<>();
            for (User u : users) {
                rows.add(new AdminItemAdapter.AdminRow(
                        u.getId(),
                        u.getName(),
                        u.getEmail() + " • Роль: " + u.getRole(),
                        false
                ));
            }
            adapter.setRows(rows);
            rvAdmin.setAdapter(adapter);
        });
    }

    private void loadBorrowingsTab() {
        repository.getAllBorrowings(borrowings -> {
            BorrowingAdapter adapter = new BorrowingAdapter(true, null);
            adapter.setBorrowings(borrowings);
            rvAdmin.setAdapter(adapter);
        });
    }

    private void showCategoryDialog(Category existing) {
        EditText etName = new EditText(this);
        etName.setHint(R.string.hint_category_name);
        if (existing != null) etName.setText(existing.getName());

        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(48, 24, 48, 8);
        layout.addView(etName);

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.dialog_add_category : R.string.dialog_edit_category)
                .setView(layout)
                .setPositiveButton(R.string.btn_save, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    if (existing == null) {
                        repository.addCategory(name, ok -> refreshCurrentTab());
                    } else {
                        repository.updateCategory(existing.getId(), name, ok -> refreshCurrentTab());
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showBookDialog(Book existing) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 8);

        EditText etTitle = new EditText(this);
        etTitle.setHint(R.string.hint_book_title);

        EditText etAuthor = new EditText(this);
        etAuthor.setHint(R.string.hint_book_author);

        EditText etDesc = new EditText(this);
        etDesc.setHint(R.string.hint_book_desc);

        Spinner spinnerCat = new Spinner(this);
        ArrayAdapter<Category> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, cachedCategories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(catAdapter);

        EditText etCopies = new EditText(this);
        etCopies.setHint(R.string.hint_book_copies);
        etCopies.setInputType(InputType.TYPE_CLASS_NUMBER);

        EditText etCover = new EditText(this);
        etCover.setHint(R.string.hint_book_cover);
        etCover.setInputType(InputType.TYPE_CLASS_NUMBER);

        if (existing != null) {
            etTitle.setText(existing.getTitle());
            etAuthor.setText(existing.getAuthor());
            etDesc.setText(existing.getDescription());
            etCopies.setText(String.valueOf(existing.getTotalCopies()));
            etCover.setText(String.valueOf(existing.getCoverResId()));
            for (int i = 0; i < cachedCategories.size(); i++) {
                if (cachedCategories.get(i).getId() == existing.getCategoryId()) {
                    spinnerCat.setSelection(i);
                    break;
                }
            }
        } else {
            etCopies.setText("3");
            etCover.setText("1");
        }

        layout.addView(etTitle);
        layout.addView(etAuthor);
        layout.addView(etDesc);
        layout.addView(spinnerCat);
        layout.addView(etCopies);
        layout.addView(etCover);

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? R.string.dialog_add_book : R.string.dialog_edit_book)
                .setView(layout)
                .setPositiveButton(R.string.btn_save, (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String author = etAuthor.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    if (title.isEmpty() || author.isEmpty()) {
                        Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int copies = 1;
                    int cover = 1;
                    try {
                        copies = Math.max(1, Integer.parseInt(etCopies.getText().toString().trim()));
                        cover = Math.min(6, Math.max(1, Integer.parseInt(etCover.getText().toString().trim())));
                    } catch (NumberFormatException ignored) {}

                    Category selectedCat = (Category) spinnerCat.getSelectedItem();
                    int catId = selectedCat != null ? selectedCat.getId() : 0;

                    if (existing == null) {
                        repository.addBook(title, author, desc, catId, copies, cover, ok -> refreshCurrentTab());
                    } else {
                        int diff = copies - existing.getTotalCopies();
                        int newAvail = Math.max(0, existing.getAvailableCopies() + diff);
                        repository.updateBook(existing.getId(), title, author, desc, catId,
                                copies, newAvail, cover, ok -> refreshCurrentTab());
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }
}