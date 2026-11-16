package com.example.libraryapp.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.example.libraryapp.database.DatabaseHelper;
import com.example.libraryapp.models.Book;
import com.example.libraryapp.models.Borrowing;
import com.example.libraryapp.models.Category;
import com.example.libraryapp.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryRepository {

    public interface DataCallback<T> {
        void onComplete(T result);
    }

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public LibraryRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ==================== ПОЛЬЗОВАТЕЛИ И АВТОРИЗАЦИЯ ====================

    public void login(String email, String password, DataCallback<User> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            User user = null;
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    DatabaseHelper.COL_USER_EMAIL + "=? AND " + DatabaseHelper.COL_USER_PASSWORD + "=?",
                    new String[]{email.trim(), password.trim()},
                    null, null, null
            );
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    user = new User(
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE))
                    );
                }
                cursor.close();
            }
            User finalUser = user;
            mainHandler.post(() -> callback.onComplete(finalUser));
        });
    }

    public void register(String name, String email, String password, DataCallback<User> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor check = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COL_USER_ID},
                    DatabaseHelper.COL_USER_EMAIL + "=?",
                    new String[]{email.trim()},
                    null, null, null
            );
            boolean exists = false;
            if (check != null) {
                exists = check.moveToFirst();
                check.close();
            }
            if (exists) {
                mainHandler.post(() -> callback.onComplete(null));
                return;
            }

            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_USER_NAME, name.trim());
            cv.put(DatabaseHelper.COL_USER_EMAIL, email.trim());
            cv.put(DatabaseHelper.COL_USER_PASSWORD, password.trim());
            cv.put(DatabaseHelper.COL_USER_ROLE, "user");

            long newId = db.insert(DatabaseHelper.TABLE_USERS, null, cv);
            User createdUser = newId != -1
                    ? new User((int) newId, name.trim(), email.trim(), password.trim(), "user")
                    : null;

            mainHandler.post(() -> callback.onComplete(createdUser));
        });
    }

    public void getAllUsers(DataCallback<List<User>> callback) {
        executor.execute(() -> {
            List<User> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null,
                    DatabaseHelper.COL_USER_ID + " ASC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(new User(
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROLE))
                    ));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    // ==================== КАТЕГОРИИ ====================

    public void getAllCategories(DataCallback<List<Category>> callback) {
        executor.execute(() -> {
            List<Category> categories = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null, null, null, null, null,
                    DatabaseHelper.COL_CAT_NAME + " ASC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    categories.add(new Category(
                            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_NAME))
                    ));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(categories));
        });
    }

    public void addCategory(String name, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_CAT_NAME, name.trim());
            long id = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, cv);
            mainHandler.post(() -> callback.onComplete(id != -1));
        });
    }

    public void updateCategory(int id, String newName, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_CAT_NAME, newName.trim());
            int rows = db.update(DatabaseHelper.TABLE_CATEGORIES, cv,
                    DatabaseHelper.COL_CAT_ID + "=?", new String[]{String.valueOf(id)});
            mainHandler.post(() -> callback.onComplete(rows > 0));
        });
    }

    public void deleteCategory(int id, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.delete(DatabaseHelper.TABLE_CATEGORIES,
                    DatabaseHelper.COL_CAT_ID + "=?", new String[]{String.valueOf(id)});
            mainHandler.post(() -> callback.onComplete(rows > 0));
        });
    }

    // ==================== КНИГИ ====================

    public void getBooks(String query, int categoryId, DataCallback<List<Book>> callback) {
        executor.execute(() -> {
            List<Book> books = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            StringBuilder sql = new StringBuilder(
                    "SELECT b.*, c." + DatabaseHelper.COL_CAT_NAME + " AS cat_name " +
                            "FROM " + DatabaseHelper.TABLE_BOOKS + " b " +
                            "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                            "ON b." + DatabaseHelper.COL_BOOK_CAT_ID + " = c." + DatabaseHelper.COL_CAT_ID + " " +
                            "WHERE 1=1 "
            );
            List<String> args = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                sql.append("AND (LOWER(b.").append(DatabaseHelper.COL_BOOK_TITLE)
                        .append(") LIKE ? OR LOWER(b.").append(DatabaseHelper.COL_BOOK_AUTHOR)
                        .append(") LIKE ?) ");
                String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                args.add(pattern);
                args.add(pattern);
            }

            if (categoryId > 0) {
                sql.append("AND b.").append(DatabaseHelper.COL_BOOK_CAT_ID).append(" = ? ");
                args.add(String.valueOf(categoryId));
            }

            sql.append("ORDER BY b.").append(DatabaseHelper.COL_BOOK_ID).append(" DESC");

            Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    books.add(cursorToBook(cursor));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(books));
        });
    }

    public void getBookById(int bookId, DataCallback<Book> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT b.*, c." + DatabaseHelper.COL_CAT_NAME + " AS cat_name " +
                    "FROM " + DatabaseHelper.TABLE_BOOKS + " b " +
                    "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                    "ON b." + DatabaseHelper.COL_BOOK_CAT_ID + " = c." + DatabaseHelper.COL_CAT_ID + " " +
                    "WHERE b." + DatabaseHelper.COL_BOOK_ID + " = ?";
            Book book = null;
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(bookId)});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    book = cursorToBook(cursor);
                }
                cursor.close();
            }
            Book finalBook = book;
            mainHandler.post(() -> callback.onComplete(finalBook));
        });
    }

    public void addBook(String title, String author, String desc, int categoryId,
                        int copies, int coverRes, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_BOOK_TITLE, title.trim());
            cv.put(DatabaseHelper.COL_BOOK_AUTHOR, author.trim());
            cv.put(DatabaseHelper.COL_BOOK_DESC, desc.trim());
            if (categoryId > 0) {
                cv.put(DatabaseHelper.COL_BOOK_CAT_ID, categoryId);
            }
            cv.put(DatabaseHelper.COL_BOOK_TOTAL, copies);
            cv.put(DatabaseHelper.COL_BOOK_AVAILABLE, copies);
            cv.put(DatabaseHelper.COL_BOOK_COVER, coverRes);
            long id = db.insert(DatabaseHelper.TABLE_BOOKS, null, cv);
            mainHandler.post(() -> callback.onComplete(id != -1));
        });
    }

    public void updateBook(int bookId, String title, String author, String desc, int categoryId,
                           int totalCopies, int availableCopies, int coverRes, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_BOOK_TITLE, title.trim());
            cv.put(DatabaseHelper.COL_BOOK_AUTHOR, author.trim());
            cv.put(DatabaseHelper.COL_BOOK_DESC, desc.trim());
            if (categoryId > 0) {
                cv.put(DatabaseHelper.COL_BOOK_CAT_ID, categoryId);
            }
            cv.put(DatabaseHelper.COL_BOOK_TOTAL, totalCopies);
            cv.put(DatabaseHelper.COL_BOOK_AVAILABLE, availableCopies);
            cv.put(DatabaseHelper.COL_BOOK_COVER, coverRes);
            int rows = db.update(DatabaseHelper.TABLE_BOOKS, cv,
                    DatabaseHelper.COL_BOOK_ID + "=?", new String[]{String.valueOf(bookId)});
            mainHandler.post(() -> callback.onComplete(rows > 0));
        });
    }

    public void deleteBook(int bookId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.delete(DatabaseHelper.TABLE_BOOKS,
                    DatabaseHelper.COL_BOOK_ID + "=?", new String[]{String.valueOf(bookId)});
            mainHandler.post(() -> callback.onComplete(rows > 0));
        });
    }

    private Book cursorToBook(Cursor cursor) {
        String catName = cursor.getString(cursor.getColumnIndexOrThrow("cat_name"));
        if (catName == null) catName = "Без категории";
        return new Book(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_AUTHOR)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_DESC)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_CAT_ID)),
                catName,
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_TOTAL)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_AVAILABLE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_COVER))
        );
    }

    // ==================== ИЗБРАННОЕ ====================

    public void isFavorite(int userId, int bookId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_FAVORITES,
                    new String[]{DatabaseHelper.COL_FAV_ID},
                    DatabaseHelper.COL_FAV_USER_ID + "=? AND " + DatabaseHelper.COL_FAV_BOOK_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(bookId)},
                    null, null, null
            );
            boolean fav = false;
            if (cursor != null) {
                fav = cursor.moveToFirst();
                cursor.close();
            }
            boolean finalFav = fav;
            mainHandler.post(() -> callback.onComplete(finalFav));
        });
    }

    public void toggleFavorite(int userId, int bookId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_FAVORITES,
                    new String[]{DatabaseHelper.COL_FAV_ID},
                    DatabaseHelper.COL_FAV_USER_ID + "=? AND " + DatabaseHelper.COL_FAV_BOOK_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(bookId)},
                    null, null, null
            );
            boolean exists = false;
            if (cursor != null) {
                exists = cursor.moveToFirst();
                cursor.close();
            }

            boolean nowFavorite;
            if (exists) {
                db.delete(DatabaseHelper.TABLE_FAVORITES,
                        DatabaseHelper.COL_FAV_USER_ID + "=? AND " + DatabaseHelper.COL_FAV_BOOK_ID + "=?",
                        new String[]{String.valueOf(userId), String.valueOf(bookId)});
                nowFavorite = false;
            } else {
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_FAV_USER_ID, userId);
                cv.put(DatabaseHelper.COL_FAV_BOOK_ID, bookId);
                db.insert(DatabaseHelper.TABLE_FAVORITES, null, cv);
                nowFavorite = true;
            }
            mainHandler.post(() -> callback.onComplete(nowFavorite));
        });
    }

    public void getFavoriteBooks(int userId, DataCallback<List<Book>> callback) {
        executor.execute(() -> {
            List<Book> books = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String sql = "SELECT b.*, c." + DatabaseHelper.COL_CAT_NAME + " AS cat_name " +
                    "FROM " + DatabaseHelper.TABLE_BOOKS + " b " +
                    "INNER JOIN " + DatabaseHelper.TABLE_FAVORITES + " f " +
                    "ON b." + DatabaseHelper.COL_BOOK_ID + " = f." + DatabaseHelper.COL_FAV_BOOK_ID + " " +
                    "LEFT JOIN " + DatabaseHelper.TABLE_CATEGORIES + " c " +
                    "ON b." + DatabaseHelper.COL_BOOK_CAT_ID + " = c." + DatabaseHelper.COL_CAT_ID + " " +
                    "WHERE f." + DatabaseHelper.COL_FAV_USER_ID + " = ? " +
                    "ORDER BY f." + DatabaseHelper.COL_FAV_ID + " DESC";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(userId)});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    books.add(cursorToBook(cursor));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(books));
        });
    }

    // ==================== ВЫДАЧА И ВОЗВРАТ КНИГ ====================

    public void isBookBorrowedByUser(int userId, int bookId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_BORROWINGS,
                    new String[]{DatabaseHelper.COL_BORROW_ID},
                    DatabaseHelper.COL_BORROW_USER_ID + "=? AND " +
                            DatabaseHelper.COL_BORROW_BOOK_ID + "=? AND " +
                            DatabaseHelper.COL_BORROW_STATUS + "='borrowed'",
                    new String[]{String.valueOf(userId), String.valueOf(bookId)},
                    null, null, null
            );
            boolean borrowed = false;
            if (cursor != null) {
                borrowed = cursor.moveToFirst();
                cursor.close();
            }
            boolean finalBorrowed = borrowed;
            mainHandler.post(() -> callback.onComplete(finalBorrowed));
        });
    }

    public void borrowBook(int userId, int bookId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            boolean success = false;
            try {
                Cursor cursor = db.query(
                        DatabaseHelper.TABLE_BOOKS,
                        new String[]{DatabaseHelper.COL_BOOK_AVAILABLE},
                        DatabaseHelper.COL_BOOK_ID + "=?",
                        new String[]{String.valueOf(bookId)},
                        null, null, null
                );
                int available = 0;
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        available = cursor.getInt(0);
                    }
                    cursor.close();
                }

                if (available > 0) {
                    String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COL_BORROW_USER_ID, userId);
                    cv.put(DatabaseHelper.COL_BORROW_BOOK_ID, bookId);
                    cv.put(DatabaseHelper.COL_BORROW_DATE, today);
                    cv.put(DatabaseHelper.COL_BORROW_STATUS, "borrowed");
                    db.insert(DatabaseHelper.TABLE_BORROWINGS, null, cv);

                    ContentValues bookCv = new ContentValues();
                    bookCv.put(DatabaseHelper.COL_BOOK_AVAILABLE, available - 1);
                    db.update(DatabaseHelper.TABLE_BOOKS, bookCv,
                            DatabaseHelper.COL_BOOK_ID + "=?", new String[]{String.valueOf(bookId)});

                    db.setTransactionSuccessful();
                    success = true;
                }
            } finally {
                db.endTransaction();
            }
            boolean finalSuccess = success;
            mainHandler.post(() -> callback.onComplete(finalSuccess));
        });
    }

    public void returnBook(int borrowingId, int bookId, DataCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            boolean success = false;
            try {
                String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_RETURN_DATE, today);
                cv.put(DatabaseHelper.COL_BORROW_STATUS, "returned");
                int updated = db.update(DatabaseHelper.TABLE_BORROWINGS, cv,
                        DatabaseHelper.COL_BORROW_ID + "=? AND " + DatabaseHelper.COL_BORROW_STATUS + "='borrowed'",
                        new String[]{String.valueOf(borrowingId)});

                if (updated > 0) {
                    db.execSQL("UPDATE " + DatabaseHelper.TABLE_BOOKS +
                            " SET " + DatabaseHelper.COL_BOOK_AVAILABLE + " = " + DatabaseHelper.COL_BOOK_AVAILABLE + " + 1" +
                            " WHERE " + DatabaseHelper.COL_BOOK_ID + " = ?", new Object[]{bookId});
                    db.setTransactionSuccessful();
                    success = true;
                }
            } finally {
                db.endTransaction();
            }
            boolean finalSuccess = success;
            mainHandler.post(() -> callback.onComplete(finalSuccess));
        });
    }

    public void getUserBorrowings(int userId, DataCallback<List<Borrowing>> callback) {
        executor.execute(() -> {
            List<Borrowing> list = queryBorrowings(
                    "WHERE br." + DatabaseHelper.COL_BORROW_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    public void getAllBorrowings(DataCallback<List<Borrowing>> callback) {
        executor.execute(() -> {
            List<Borrowing> list = queryBorrowings("", new String[0]);
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    private List<Borrowing> queryBorrowings(String whereClause, String[] args) {
        List<Borrowing> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT br.*, u." + DatabaseHelper.COL_USER_NAME + " AS user_name, " +
                "b." + DatabaseHelper.COL_BOOK_TITLE + " AS book_title, " +
                "b." + DatabaseHelper.COL_BOOK_AUTHOR + " AS book_author " +
                "FROM " + DatabaseHelper.TABLE_BORROWINGS + " br " +
                "INNER JOIN " + DatabaseHelper.TABLE_USERS + " u ON br." + DatabaseHelper.COL_BORROW_USER_ID + " = u." + DatabaseHelper.COL_USER_ID + " " +
                "INNER JOIN " + DatabaseHelper.TABLE_BOOKS + " b ON br." + DatabaseHelper.COL_BORROW_BOOK_ID + " = b." + DatabaseHelper.COL_BOOK_ID + " " +
                whereClause + " ORDER BY br." + DatabaseHelper.COL_BORROW_ID + " DESC";
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(new Borrowing(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BORROW_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BORROW_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow("user_name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BORROW_BOOK_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow("book_title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("book_author")),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BORROW_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_RETURN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BORROW_STATUS))
                ));
            }
            cursor.close();
        }
        return list;
    }
}