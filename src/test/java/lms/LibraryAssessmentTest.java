package lms;

import lms.model.Book;
import lms.model.BorrowRecord;
import lms.model.Member;

public final class LibraryAssessmentTest {
    private LibraryAssessmentTest() {
    }

    public static void main(String[] args) {
        Library library = new Library();
        Book book = new Book("B001", "Object-Oriented Java", "A. Author", "Education");
        Member member = new Member("M001", "Test Member", "0123456789");
        library.addBook(book);
        library.addMember(member);

        check(library.searchBooks("object").size() == 1, "book title search should be case-insensitive");
        check(library.searchMembers("0123").size() == 1, "member contact search should work");
        expectFailure(() -> library.addBook(new Book("b001", "Duplicate", "A. Author", "Education")),
                "duplicate book IDs should be rejected");
        expectFailure(() -> library.addMember(new Member("m001", "Duplicate", "0000000000")),
                "duplicate member IDs should be rejected");
        expectFailure(() -> new Book("", "Title", "Author", "Category"),
                "empty book IDs should be rejected");

        BorrowRecord record = library.borrowBook("M001", "B001");
        check(!book.isAvailable(), "borrowed book should become unavailable");
        check(library.getBorrowedBookCount() == 1, "dashboard borrowed count should update");
        check(library.getActiveBorrowRecords().size() == 1,
                "borrowing should create an active borrowing record");
        expectFailure(() -> library.borrowBook("M001", "B001"),
                "a borrowed book should not be borrowed twice");
        expectFailure(() -> library.removeBook("B001"),
                "an actively borrowed book should not be removed");
        expectFailure(() -> library.borrowBook("M999", "B001"),
                "unknown members should not be able to borrow");

        library.returnBookByBookId(book.getBookId());
        check(book.isAvailable(), "returned book should become available");
        check(record.getReturnDate() != null, "return should update the borrowing record");
        check(library.getActiveBorrowRecords().isEmpty(), "returned record should no longer be active");
        expectFailure(() -> library.returnBook(record.getRecordId()),
                "a returned book should not be returned twice");
        library.removeBook("B001");
        check(library.getBooks().isEmpty(), "a returned book should be removable");
        check(library.getBorrowRecords().size() == 1, "completed borrow history should remain recorded");

        Library demo = new Library();
        SampleData.populate(demo);
        check(demo.getBooks().size() == 8, "demo startup should provide eight sample books");
        check(demo.getMembers().size() == 11, "demo startup should provide eleven sample members");
        System.out.println("LibraryAssessmentTest passed.");
    }

    private static void expectFailure(Runnable action, String message) {
        try {
            action.run();
        } catch (IllegalStateException | IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
