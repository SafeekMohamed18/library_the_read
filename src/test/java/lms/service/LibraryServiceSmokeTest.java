package lms.service;

import lms.model.Book;
import lms.model.Rental;
import lms.model.Member;
import lms.repository.CsvLibraryStore;
import lms.repository.CsvLibraryStore.Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

public final class LibraryServiceSmokeTest {
    private LibraryServiceSmokeTest() { }

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("the-read-test-");
        try {
            LibraryService service = new LibraryService(new CsvLibraryStore(directory));
            char[] password = "assignment123".toCharArray();
            service.createFirstLibrarian("librarian", "Test Librarian", "0123456789",
                    "librarian@example.com", password);
            Arrays.fill(password, '\0');
            check(service.authenticate("librarian", "assignment123".toCharArray()) != null,
                    "librarian login should work");
            check(service.authenticate("librarian", "wrong-password".toCharArray()) == null,
                    "wrong password should be rejected");

            Book book = new Book("9781234567890", "A, \"Great\" Book", "A. Writer", "Fiction",
                    "Example Press", 2024, 500, 5, 5);
            service.addBook(book);
            service.addMember(new Member("M-100", "Test Member", "9876543210", "member@example.com",
                    "10 Main Street, City", "Student", LocalDate.now(), true));
            Rental first = service.issueBook("M-100", book.getIsbn());
            service.issueBook("M-100", book.getIsbn());
            service.issueBook("M-100", book.getIsbn());
            expectFailure(() -> service.issueBook("M-100", book.getIsbn()),
                    "the fourth active rental should be rejected");
            check(service.findBook(book.getIsbn()).getAvailableCopies() == 2,
                    "available copies should decrease as rentals are created");
            check(service.getActiveRentals().size() == 3, "three active rentals should be recorded");

            LibraryService reopened = new LibraryService(new CsvLibraryStore(directory));
            check(reopened.findBook(book.getIsbn()).getTitle().equals(book.getTitle()),
                    "quoted and comma-separated book data should round-trip through CSV");
            check(reopened.findMember("M-100").getAddress().equals("10 Main Street, City"),
                    "member address should survive a restart");
            check(reopened.getActiveRentals().size() == 3, "rentals should survive a restart");
            reopened.returnBook(first.getRentalId());
            check(reopened.findBook(book.getIsbn()).getAvailableCopies() == 3,
                    "returning a book should restore one available copy");
            check(Files.exists(directory.resolve("books.csv.bak")),
                    "saving again should retain a backup of the previous CSV");
            expectFailure(() -> reopened.deleteBook(book.getIsbn()),
                    "a book with active rentals should not be removed");
            for (Rental rental : reopened.getActiveRentals()) reopened.returnBook(rental.getRentalId());
            reopened.deleteBook(book.getIsbn());
            check(reopened.getBooks().isEmpty(), "a removed book should no longer appear in the catalogue");
            check(reopened.getMemberHistory("M-100").size() == 3,
                    "removing a book should preserve its rental history");
            LibraryService afterArchive = new LibraryService(new CsvLibraryStore(directory));
            check(afterArchive.getBooks().isEmpty(), "catalogue removal should survive a restart");
            check(afterArchive.getMemberHistory("M-100").size() == 3,
                    "archived book history should remain available after a restart");
            verifyFinePolicy(directory.resolve("fine-policy"));
            verifyLegacyLoanMigration(directory.resolve("legacy-migration"));
            System.out.println("LibraryServiceSmokeTest passed.");
        } finally {
            try (var files = Files.list(directory)) {
                for (Path file : files.toList()) Files.deleteIfExists(file);
            }
            Files.deleteIfExists(directory);
        }
    }

    private static void verifyFinePolicy(Path directory) throws Exception {
        LibraryService service = new LibraryService(new CsvLibraryStore(directory));
        Book book = new Book("9781111111111", "Fine Test", "Test Author", "Testing",
                "Example Press", 2024, 15, 1, 1);
        service.addBook(book);
        service.addMember(new Member("M-200", "Fine Member", "1234567890", "fine@example.com",
                "1 Test Road", "Staff", LocalDate.now(), true));
        Rental rental = service.issueBook("M-200", book.getIsbn());

        Path rentalsFile = directory.resolve("rentals.csv");
        String csv = Files.readString(rentalsFile);
        String futureDueDate = LocalDate.now().plusDays(LibraryService.RENTAL_DAYS).toString();
        Files.writeString(rentalsFile, csv.replace(futureDueDate, LocalDate.now().minusDays(2).toString()));

        LibraryService overdueService = new LibraryService(new CsvLibraryStore(directory));
        double fine = overdueService.returnBook(rental.getRentalId());
        check(fine == 15.0, "overdue fine should be capped at the book price");
        expectFailure(() -> overdueService.issueBook("M-200", book.getIsbn()),
                "an unpaid fine should block another issue");
        overdueService.payFine(rental.getRentalId());
        overdueService.issueBook("M-200", book.getIsbn());

        try (var files = Files.list(directory)) {
            for (Path file : files.toList()) Files.deleteIfExists(file);
        }
        Files.deleteIfExists(directory);
    }

    private static void verifyLegacyLoanMigration(Path directory) throws Exception {
        CsvLibraryStore store = new CsvLibraryStore(directory);
        Book book = new Book("9782222222222", "Legacy Test", "Test Author", "Testing",
                "Example Press", 2024, 100, 1, 0);
        Member member = new Member("M-300", "Legacy Member", "1234567891", "legacy@example.com",
                "2 Test Road", "Student", LocalDate.now(), true);
        Rental rental = new Rental("legacy-rental", book.getIsbn(), member.getId(),
                LocalDate.now(), LocalDate.now().plusDays(14), null, 0, true, false);
        store.save(new Data(java.util.List.of(book), java.util.List.of(member),
                java.util.List.of(rental), java.util.List.of()));

        Path rentalsFile = directory.resolve("rentals.csv");
        String legacyData = Files.readString(rentalsFile).replaceFirst("rentalId", "loanId");
        Files.writeString(directory.resolve("loans.csv"), legacyData);
        Files.delete(rentalsFile);

        LibraryService migrated = new LibraryService(new CsvLibraryStore(directory));
        check(migrated.getActiveRentals().size() == 1,
                "existing loans.csv data should load as rentals");
        migrated.returnBook("legacy-rental");
        check(Files.readString(rentalsFile).startsWith("rentalId,"),
                "saving migrated data should write the rentals.csv format");
        check(new LibraryService(new CsvLibraryStore(directory)).getRentals().size() == 1,
                "migrated rentals should load after restarting");

        try (var files = Files.list(directory)) {
            for (Path file : files.toList()) Files.deleteIfExists(file);
        }
        Files.deleteIfExists(directory);
    }

    private static void expectFailure(CheckedAction action, String message) throws Exception {
        try {
            action.run();
        } catch (IllegalStateException | IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }
}
