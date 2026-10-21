package lms.model;

public final class Book {
    private final String bookId;
    private String title;
    private String author;
    private String category;
    private boolean available;

    public Book(String bookId, String title, String author, String category) {
        this.bookId = requireText(bookId, "Book ID");
        setTitle(title);
        setAuthor(author);
        setCategory(category);
        available = true;
    }

    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = requireText(title, "Title");
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = requireText(author, "Author");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = requireText(category, "Category");
    }

    public boolean isAvailable() {
        return available;
    }

    public String getAvailabilityStatus() {
        return available ? "Available" : "Borrowed";
    }

    public void borrow() {
        if (!available) {
            throw new IllegalStateException("This book is already borrowed.");
        }
        available = false;
    }

    public void returnToLibrary() {
        if (available) {
            throw new IllegalStateException("This book is already available.");
        }
        available = true;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }
}
