package com.example.libraryapp.activities;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.libraryapp.R;
import com.example.libraryapp.models.Book;
import com.example.libraryapp.repository.LibraryRepository;
import com.example.libraryapp.utils.CoverHelper;
import com.example.libraryapp.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK_ID = "extra_book_id";

    private LibraryRepository repository;
    private SessionManager sessionManager;
    private int bookId;
    private Book currentBook;

    private FrameLayout coverFrame;
    private TextView tvCoverLetter, tvCategory, tvTitle, tvAuthor, tvAvailability, tvDescription;
    private MaterialButton btnBorrow, btnFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        repository = new LibraryRepository(this);
        sessionManager = new SessionManager(this);
        bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, -1);

        if (bookId == -1) {
            finish();
            return;
        }

        MaterialButton btnBack = findViewById(R.id.btnBackDetail);
        coverFrame = findViewById(R.id.detailCoverFrame);
        tvCoverLetter = findViewById(R.id.tvDetailCoverLetter);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAuthor = findViewById(R.id.tvDetailAuthor);
        tvAvailability = findViewById(R.id.tvDetailAvailability);
        tvDescription = findViewById(R.id.tvDetailDescription);
        btnBorrow = findViewById(R.id.btnBorrowBook);
        btnFavorite = findViewById(R.id.btnToggleFavorite);

        btnBack.setOnClickListener(v -> finish());

        btnBorrow.setOnClickListener(v -> {
            if (currentBook == null || currentBook.getAvailableCopies() <= 0) return;
            btnBorrow.setEnabled(false);
            repository.borrowBook(sessionManager.getUserId(), bookId, success -> {
                if (success) {
                    Toast.makeText(this, R.string.msg_book_borrowed, Toast.LENGTH_LONG).show();
                    loadBookDetails();
                } else {
                    btnBorrow.setEnabled(true);
                }
            });
        });

        btnFavorite.setOnClickListener(v ->
                repository.toggleFavorite(sessionManager.getUserId(), bookId, isNowFav -> {
                    Toast.makeText(this,
                            isNowFav ? R.string.msg_added_favorite : R.string.msg_removed_favorite,
                            Toast.LENGTH_SHORT).show();
                    updateFavoriteButton(isNowFav);
                }));

        loadBookDetails();
    }

    private void loadBookDetails() {
        repository.getBookById(bookId, book -> {
            if (book == null) {
                finish();
                return;
            }
            currentBook = book;

            coverFrame.setBackgroundResource(CoverHelper.getCoverDrawable(book.getCoverResId()));
            String firstLetter = (book.getTitle() != null && !book.getTitle().isEmpty())
                    ? book.getTitle().substring(0, 1).toUpperCase()
                    : "К";
            tvCoverLetter.setText(firstLetter);
            tvCategory.setText(book.getCategoryName());
            tvTitle.setText(book.getTitle());
            tvAuthor.setText(book.getAuthor());
            tvDescription.setText(book.getDescription());

            if (book.getAvailableCopies() > 0) {
                tvAvailability.setText(getString(R.string.status_available_text, book.getAvailableCopies()));
                tvAvailability.setTextColor(ContextCompat.getColor(this, R.color.status_available));
            } else {
                tvAvailability.setText(R.string.status_unavailable_text);
                tvAvailability.setTextColor(ContextCompat.getColor(this, R.color.status_unavailable));
            }

            checkBorrowStatus();
            repository.isFavorite(sessionManager.getUserId(), bookId, this::updateFavoriteButton);
        });
    }

    private void checkBorrowStatus() {
        repository.isBookBorrowedByUser(sessionManager.getUserId(), bookId, isBorrowed -> {
            if (isBorrowed) {
                btnBorrow.setEnabled(false);
                btnBorrow.setText(R.string.btn_already_borrowed);
            } else if (currentBook.getAvailableCopies() > 0) {
                btnBorrow.setEnabled(true);
                btnBorrow.setText(R.string.btn_borrow);
            } else {
                btnBorrow.setEnabled(false);
                btnBorrow.setText(R.string.status_unavailable_text);
            }
        });
    }

    private void updateFavoriteButton(boolean isFavorite) {
        btnFavorite.setText(isFavorite ? R.string.btn_remove_favorite : R.string.btn_add_favorite);
    }
}