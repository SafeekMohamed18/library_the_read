package lms.model;

import java.time.LocalDate;

public final class Rental {
    private final String rentalId;
    private final String isbn;
    private final String memberId;
    private final LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fineAmount;
    private boolean finePaid;
    private boolean renewed;

    public Rental(String rentalId, String isbn, String memberId, LocalDate issueDate, LocalDate dueDate,
                LocalDate returnDate, double fineAmount, boolean finePaid, boolean renewed) {
        this.rentalId = requireText(rentalId, "Rental ID");
        this.isbn = requireText(isbn, "ISBN");
        this.memberId = requireText(memberId, "Member ID");
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.finePaid = finePaid;
        this.renewed = renewed;
    }

    public String getRentalId() {
         return rentalId; 
        }
    public String getIsbn() { return isbn; }
    public String getMemberId() { return memberId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getFineAmount() { return fineAmount; }
    public boolean isFinePaid() { return finePaid; }
    public boolean isRenewed() { return renewed; }
    public boolean isActive() { return returnDate == null; }

    public void renew(LocalDate newDueDate) {
        if (renewed || !isActive()) {
            throw new IllegalStateException("This rental cannot be renewed.");
        }
        dueDate = newDueDate;
        renewed = true;
    }

    public void close(LocalDate date, double fine) {
        if (!isActive()) {
            throw new IllegalStateException("This rental has already been returned.");
        }
        returnDate = date;
        fineAmount = fine;
        finePaid = fine == 0;
    }

    public void markFinePaid() {
        if (fineAmount <= 0) {
            throw new IllegalStateException("There is no outstanding fine for this rental.");
        }
        finePaid = true;
    }

    public void reopenForRollback() {
        returnDate = null;
        fineAmount = 0;
        finePaid = true;
    }

    public void restoreDueDate(LocalDate previousDueDate) {
        dueDate = previousDueDate;
        renewed = false;
    }

    public void setFinePaidForRollback(boolean finePaid) {
        this.finePaid = finePaid;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }
}
