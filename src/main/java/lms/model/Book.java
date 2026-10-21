package lms.model;

public final class Book extends LibraryItem {
    private boolean active = true;

    public Book(String isbn, String title, String author, String category, String publisher,
                int year, double price, int totalCopies, int availableCopies) {
        super(isbn, title, author, category, publisher, year, price, totalCopies, availableCopies);
        if (!isbn.matches("\\d{10}|\\d{13}")) {
            throw new IllegalArgumentException("ISBN must contain 10 or 13 digits.");
        }
    }

    public Book(String isbn, String title, String author, String category, String publisher,
                int year, double price, int totalCopies, int availableCopies, boolean active) {
        this(isbn, title, author, category, publisher, year, price, totalCopies, availableCopies);
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getItemType() {
        return "Book";
    }
}
