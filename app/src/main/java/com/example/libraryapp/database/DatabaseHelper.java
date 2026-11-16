package com.example.libraryapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "library.db";
    public static final int DATABASE_VERSION = 1;

    // Таблица 1: Пользователи
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_ROLE = "role";

    // Таблица 2: Категории
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_NAME = "name";

    // Таблица 3: Книги
    public static final String TABLE_BOOKS = "books";
    public static final String COL_BOOK_ID = "id";
    public static final String COL_BOOK_TITLE = "title";
    public static final String COL_BOOK_AUTHOR = "author";
    public static final String COL_BOOK_DESC = "description";
    public static final String COL_BOOK_CAT_ID = "category_id";
    public static final String COL_BOOK_TOTAL = "total_copies";
    public static final String COL_BOOK_AVAILABLE = "available_copies";
    public static final String COL_BOOK_COVER = "cover_res_id";

    // Таблица 4: Выдача и возврат книг
    public static final String TABLE_BORROWINGS = "borrowings";
    public static final String COL_BORROW_ID = "id";
    public static final String COL_BORROW_USER_ID = "user_id";
    public static final String COL_BORROW_BOOK_ID = "book_id";
    public static final String COL_BORROW_DATE = "borrow_date";
    public static final String COL_RETURN_DATE = "return_date";
    public static final String COL_BORROW_STATUS = "status";

    // Таблица 5: Избранное
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COL_FAV_ID = "id";
    public static final String COL_FAV_USER_ID = "user_id";
    public static final String COL_FAV_BOOK_ID = "book_id";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_NAME + " TEXT NOT NULL, " +
                COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                COL_USER_PASSWORD + " TEXT NOT NULL, " +
                COL_USER_ROLE + " TEXT NOT NULL DEFAULT 'user'" +
                ");";

        String createCategories = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CAT_NAME + " TEXT UNIQUE NOT NULL" +
                ");";

        String createBooks = "CREATE TABLE " + TABLE_BOOKS + " (" +
                COL_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BOOK_TITLE + " TEXT NOT NULL, " +
                COL_BOOK_AUTHOR + " TEXT NOT NULL, " +
                COL_BOOK_DESC + " TEXT, " +
                COL_BOOK_CAT_ID + " INTEGER, " +
                COL_BOOK_TOTAL + " INTEGER NOT NULL DEFAULT 1, " +
                COL_BOOK_AVAILABLE + " INTEGER NOT NULL DEFAULT 1, " +
                COL_BOOK_COVER + " INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY(" + COL_BOOK_CAT_ID + ") REFERENCES " +
                TABLE_CATEGORIES + "(" + COL_CAT_ID + ") ON DELETE SET NULL" +
                ");";

        String createBorrowings = "CREATE TABLE " + TABLE_BORROWINGS + " (" +
                COL_BORROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BORROW_USER_ID + " INTEGER NOT NULL, " +
                COL_BORROW_BOOK_ID + " INTEGER NOT NULL, " +
                COL_BORROW_DATE + " TEXT NOT NULL, " +
                COL_RETURN_DATE + " TEXT, " +
                COL_BORROW_STATUS + " TEXT NOT NULL DEFAULT 'borrowed', " +
                "FOREIGN KEY(" + COL_BORROW_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COL_BORROW_BOOK_ID + ") REFERENCES " +
                TABLE_BOOKS + "(" + COL_BOOK_ID + ") ON DELETE CASCADE" +
                ");";

        String createFavorites = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COL_FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FAV_USER_ID + " INTEGER NOT NULL, " +
                COL_FAV_BOOK_ID + " INTEGER NOT NULL, " +
                "UNIQUE(" + COL_FAV_USER_ID + ", " + COL_FAV_BOOK_ID + "), " +
                "FOREIGN KEY(" + COL_FAV_USER_ID + ") REFERENCES " +
                TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COL_FAV_BOOK_ID + ") REFERENCES " +
                TABLE_BOOKS + "(" + COL_BOOK_ID + ") ON DELETE CASCADE" +
                ");";

        db.execSQL(createUsers);
        db.execSQL(createCategories);
        db.execSQL(createBooks);
        db.execSQL(createBorrowings);
        db.execSQL(createFavorites);

        seedInitialData(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // Демо-пользователи (Администратор и Читатель)
        insertUser(db, "Администратор Библиотеки", "admin@library.ru", "admin123", "admin");
        insertUser(db, "Иван Петров", "user@library.ru", "1234", "user");

        // Начальные категории
        long catClassic = insertCategory(db, "Классика");
        long catSciFi = insertCategory(db, "Фантастика");
        long catProgramming = insertCategory(db, "Программирование");
        long catPsychology = insertCategory(db, "Психология");
        long catHistory = insertCategory(db, "История");

        // Начальный каталог книг
        insertBook(db, "Мастер и Маргарита", "Михаил Булгаков",
                "Культовый роман о визите Воланда и его свиты в Москву 1930-х годов, переплетённый с историей Понтия Пилата и вечной любви.",
                catClassic, 4, 4, 1);

        insertBook(db, "Преступление и наказание", "Фёдор Достоевский",
                "Глубокий социально-психологический роман о бывшем студенте Родионе Раскольникове, его теории и пути к духовному возрождению.",
                catClassic, 3, 3, 2);

        insertBook(db, "Дюна", "Фрэнк Герберт",
                "Масштабная научно-фантастическая сага о пустынной планете Арракис, где добывается самое ценное вещество во Вселенной.",
                catSciFi, 5, 5, 3);

        insertBook(db, "1984", "Джордж Оруэлл",
                "Знаменитая антиутопия о тоталитарном обществе, Большом Брате и борьбе человека за право мыслить свободно.",
                catSciFi, 2, 2, 5);

        insertBook(db, "Философия Java", "Брюс Эккель",
                "Фундаментальное руководство по объектно-ориентированному программированию на языке Java с подробными примерами.",
                catProgramming, 3, 3, 4);

        insertBook(db, "Чистый код", "Роберт Мартин",
                "Практические советы и принципы написания понятного, поддерживаемого и качественного программного кода.",
                catProgramming, 2, 2, 6);

        insertBook(db, "Думай медленно… решай быстро", "Даниэль Канеман",
                "Книга нобелевского лауреата о двух системах мышления человека, когнитивных искажениях и принятии решений.",
                catPsychology, 3, 3, 1);

        insertBook(db, "Sapiens. Краткая история человечества", "Юваль Ной Харари",
                "Увлекательный рассказ о том, как Homo sapiens прошёл путь от древних охотников до создателей современных цивилизаций.",
                catHistory, 4, 4, 2);
    }

    private void insertUser(SQLiteDatabase db, String name, String email, String password, String role) {
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_NAME, name);
        cv.put(COL_USER_EMAIL, email);
        cv.put(COL_USER_PASSWORD, password);
        cv.put(COL_USER_ROLE, role);
        db.insert(TABLE_USERS, null, cv);
    }

    private long insertCategory(SQLiteDatabase db, String name) {
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, name);
        return db.insert(TABLE_CATEGORIES, null, cv);
    }

    private void insertBook(SQLiteDatabase db, String title, String author, String desc,
                            long catId, int total, int available, int coverRes) {
        ContentValues cv = new ContentValues();
        cv.put(COL_BOOK_TITLE, title);
        cv.put(COL_BOOK_AUTHOR, author);
        cv.put(COL_BOOK_DESC, desc);
        cv.put(COL_BOOK_CAT_ID, catId);
        cv.put(COL_BOOK_TOTAL, total);
        cv.put(COL_BOOK_AVAILABLE, available);
        cv.put(COL_BOOK_COVER, coverRes);
        db.insert(TABLE_BOOKS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BORROWINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}