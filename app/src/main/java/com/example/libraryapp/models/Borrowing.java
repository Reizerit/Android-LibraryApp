package com.example.libraryapp.models;

public class Borrowing {
    private int id;
    private int userId;
    private String userName;
    private int bookId;
    private String bookTitle;
    private String bookAuthor;
    private String borrowDate;
    private String returnDate;
    private String status; // "borrowed" или "returned"

    public Borrowing(int id, int userId, String userName, int bookId,
                     String bookTitle, String bookAuthor,
                     String borrowDate, String returnDate, String status) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public String getBookAuthor() { return bookAuthor; }
    public String getBorrowDate() { return borrowDate; }
    public String getReturnDate() { return returnDate; }
    public String getStatus() { return status; }

    public boolean isBorrowed() {
        return "borrowed".equalsIgnoreCase(status);
    }
}