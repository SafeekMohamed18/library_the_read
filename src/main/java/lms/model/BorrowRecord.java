package lms.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class BorrowRecord {
    private final String recordId;
    private final Member member;
    private final Book book;
    private final LocalDate borrowDate;
    private LocalDate returnDate;

    public BorrowRecord(Member member, Book book) {
        this(UUID.randomUUID().toString(), member, book, LocalDate.now(), null);
    }

    private BorrowRecord(String recordId, Member member, Book book, LocalDate borrowDate,
                         LocalDate returnDate) {
        this.recordId = Objects.requireNonNull(recordId);
        this.member = Objects.requireNonNull(member);
        this.book = Objects.requireNonNull(book);
        this.borrowDate = Objects.requireNonNull(borrowDate);
        this.returnDate = returnDate;
    }

    public String getRecordId() {
        return recordId;
    }

    public Member getMember() {
        return member;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isActive() {
        return returnDate == null;
    }

    public void completeReturn() {
        if (!isActive()) {
            throw new IllegalStateException("This borrowing record has already been returned.");
        }
        returnDate = LocalDate.now();
    }
}
