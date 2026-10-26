package lms;

import lms.model.Book;
import lms.model.BorrowRecord;
import lms.model.Member;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Library {
    private final List<Book> books = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<BorrowRecord> borrowRecords = new ArrayList<>();

    public List<Book> getBooks() {
        return List.copyOf(books);
    }

    public List<Member> getMembers() {
        return List.copyOf(members);
    }

    public List<BorrowRecord> getBorrowRecords() {
        return List.copyOf(borrowRecords);
    }

    public List<BorrowRecord> getActiveBorrowRecords() {
        return borrowRecords.stream().filter(BorrowRecord::isActive)
                .sorted(Comparator.comparing(BorrowRecord::getBorrowDate).reversed()).toList();
    }

    public List<Book> searchBooks(String query) {
        String term = normalize(query);
        return books.stream()
                .filter(book -> term.isEmpty()
                        || book.getBookId().toLowerCase().contains(term)
                        || book.getTitle().toLowerCase().contains(term)
                        || book.getAuthor().toLowerCase().contains(term)
                        || book.getCategory().toLowerCase().contains(term))
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Member> searchMembers(String query) {
        String term = normalize(query);
        return members.stream()
                .filter(member -> term.isEmpty()
                        || member.getMemberId().toLowerCase().contains(term)
                        || member.getName().toLowerCase().contains(term)
                        || member.getContactNumber().toLowerCase().contains(term))
                .sorted(Comparator.comparing(Member::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public void addBook(Book book) {
        if (findBookOrNull(book.getBookId()) != null) {
            throw new IllegalArgumentException("A book with this ID already exists.");
        }
        books.add(book);
    }

    public void removeBook(String bookId) {
        Book book = requireBook(bookId);
        if (borrowRecords.stream().anyMatch(record -> record.isActive() && record.getBook() == book)) {
            throw new IllegalStateException("Return this book before removing it.");
        }
        books.remove(book);
    }

    public void addMember(Member member) {
        if (findMemberOrNull(member.getMemberId()) != null) {
            throw new IllegalArgumentException("A member with this ID already exists.");
        }
        members.add(member);
    }

    public void updateBook(String bookId, String title, String author, String category) {
        Book book = requireBook(bookId);
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
    }

    public void updateMember(String memberId, String name, String contactNumber) {
        Member member = requireMember(memberId);
        member.setName(name);
        member.setContactNumber(contactNumber);
    }

    public BorrowRecord borrowBook(String memberId, String bookId) {
        Member member = requireMember(memberId);
        Book book = requireBook(bookId);
        book.borrow();
        BorrowRecord record = new BorrowRecord(member, book);
        borrowRecords.add(record);
        return record;
    }

    public void returnBook(String recordId) {
        BorrowRecord record = borrowRecords.stream()
                .filter(item -> item.getRecordId().equals(recordId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Borrowing record was not found."));
        record.completeReturn();
        record.getBook().returnToLibrary();
    }

    public void returnBookByBookId(String bookId) {
        BorrowRecord record = getActiveBorrowRecords().stream()
                .filter(item -> item.getBook().getBookId().equalsIgnoreCase(bookId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("This book is not currently borrowed."));
        returnBook(record.getRecordId());
    }

    public Book findBook(String bookId) {
        return requireBook(bookId);
    }

    public Member findMember(String memberId) {
        return requireMember(memberId);
    }

    public long getAvailableBookCount() {
        return books.stream().filter(Book::isAvailable).count();
    }

    public long getBorrowedBookCount() {
        return books.size() - getAvailableBookCount();
    }

    private Book requireBook(String bookId) {
        Book book = findBookOrNull(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book was not found.");
        }
        return book;
    }

    private Book findBookOrNull(String bookId) {
        return books.stream().filter(book -> book.getBookId().equalsIgnoreCase(bookId))
                .findFirst().orElse(null);
    }

    private Member requireMember(String memberId) {
        Member member = findMemberOrNull(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member was not found.");
        }
        return member;
    }

    private Member findMemberOrNull(String memberId) {
        return members.stream().filter(member -> member.getMemberId().equalsIgnoreCase(memberId))
                .findFirst().orElse(null);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
