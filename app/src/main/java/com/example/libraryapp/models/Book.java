package com.example.libraryapp.models;

public class Book {
    private int id;
    private String title;
    private String author;
    private String description;
    private int categoryId;
    private String categoryName;
    private int totalCopies;
    private int availableCopies;
    private int coverResId;

    public Book(int id, String title, String author, String description,
                int categoryId, String categoryName,
                int totalCopies, int availableCopies, int coverResId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.coverResId = coverResId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }

    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    public int getCoverResId() { return coverResId; }
    public void setCoverResId(int coverResId) { this.coverResId = coverResId; }
}