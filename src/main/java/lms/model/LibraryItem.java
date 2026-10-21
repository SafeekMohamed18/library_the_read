package lms.model;

public abstract class LibraryItem {
    private final String isbn;
    private String title;
    private String author;
    private String category;
    private String publisher;
    private int year;
    private double price;
    private int totalCopies;
    private int availableCopies;

    protected LibraryItem(String isbn, String title, String author, String category, String publisher,
                          int year, double price, int totalCopies, int availableCopies) {
        this.isbn = requireText(isbn, "ISBN");
        setTitle(title);
        setAuthor(author);
        setCategory(category);
        setPublisher(publisher);
        setYear(year);
        setPrice(price);
        setTotalCopies(totalCopies);
        setAvailableCopies(availableCopies);
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getPublisher() { return publisher; }
    public int getYear() { return year; }
    public double getPrice() { return price; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public void setTitle(String title) { this.title = requireText(title, "Title"); }
    public void setAuthor(String author) { this.author = requireText(author, "Author"); }
    public void setCategory(String category) { this.category = requireText(category, "Category"); }
    public void setPublisher(String publisher) { this.publisher = requireText(publisher, "Publisher"); }

    public void setYear(int year) {
        if (year < 1000 || year > java.time.Year.now().getValue()) {
            throw new IllegalArgumentException("Year must be between 1000 and the current year.");
        }
        this.year = year;
    }

    public void setPrice(double price) {
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
    }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies < 1 || totalCopies < availableCopies) {
            throw new IllegalArgumentException("Total copies must be at least 1 and not less than available copies.");
        }
        this.totalCopies = totalCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        if (availableCopies < 0 || (totalCopies > 0 && availableCopies > totalCopies)) {
            throw new IllegalArgumentException("Available copies must be between 0 and total copies.");
        }
        this.availableCopies = availableCopies;
    }

    public abstract String getItemType();

    protected static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }
}
