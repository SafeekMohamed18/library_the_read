package lms.service;

import lms.model.Book;
import lms.model.Librarian;
import lms.model.Rental;
import lms.model.Member;
import lms.repository.CsvLibraryStore;
import lms.repository.CsvLibraryStore.Data;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class LibraryService {
    public static final int BORROW_LIMIT = 3;
    public static final int RENTAL_DAYS = 14;
    public static final int RENEWAL_DAYS = 7;
    public static final double FINE_PER_DAY = 10.0;

    private final CsvLibraryStore store;
    private final List<Book> books;
    private final List<Member> members;
    private final List<Rental> rentals;
    private final List<Librarian> librarians;

    public LibraryService(CsvLibraryStore store) throws IOException {
        this.store = store;
        Data data = store.load();
        books = new ArrayList<>(data.books());
        members = new ArrayList<>(data.members());
        rentals = new ArrayList<>(data.rentals());
        librarians = new ArrayList<>(data.librarians());
        validateLoadedData();
    }

    public boolean needsLibrarianSetup() {
        return librarians.isEmpty();
    }

    public void createFirstLibrarian(String username, String fullName, String phone, String email,
                                    char[] password) throws IOException {
        if (!librarians.isEmpty()) {
            throw new IllegalStateException("A librarian account already exists.");
        }
        String normalizedUsername = requireText(username, "Username");
        if (normalizedUsername.length() < 3) {
            throw new IllegalArgumentException("Username must contain at least 3 characters.");
        }
        validatePassword(password);
        Librarian user = new Librarian(normalizedUsername, fullName, phone, email,
                PasswordHasher.hash(password));
        librarians.add(user);
        persistOrRollback(() -> librarians.remove(user));
    }

    public Librarian authenticate(String username, char[] password) {
        return librarians.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username == null ? "" : username.trim()))
                .filter(user -> PasswordHasher.verify(password, user.getPasswordHash()))
                .findFirst()
                .orElse(null);
    }

    public List<Book> getBooks() {
        return books.stream().filter(Book::isActive)
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public List<Book> searchBooks(String query) {
        String search = query == null ? "" : query.trim().toLowerCase();
        if (search.isEmpty()) return getBooks();
        return getBooks().stream().filter(book ->
                book.getIsbn().toLowerCase().contains(search)
                        || book.getTitle().toLowerCase().contains(search)
                        || book.getAuthor().toLowerCase().contains(search)
                        || book.getCategory().toLowerCase().contains(search)).toList();
    }

    public List<Member> getMembers() {
        return members.stream().sorted(Comparator.comparing(Member::getName, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public List<Member> searchMembers(String query) {
        String search = query == null ? "" : query.trim().toLowerCase();
        if (search.isEmpty()) return getMembers();
        return getMembers().stream().filter(member ->
                member.getId().toLowerCase().contains(search)
                        || member.getName().toLowerCase().contains(search)
                        || member.getPhone().contains(search)).toList();
    }

    public List<Rental> getRentals() {
        return List.copyOf(rentals);
    }

    public List<Rental> getActiveRentals() {
        return rentals.stream().filter(Rental::isActive)
                .sorted(Comparator.comparing(Rental::getDueDate)).toList();
    }

    public List<Rental> getOverdueRentals() {
        LocalDate today = LocalDate.now();
        return getActiveRentals().stream().filter(rental -> rental.getDueDate().isBefore(today)).toList();
    }

    public List<Rental> getMemberHistory(String memberId) {
        return rentals.stream().filter(rental -> rental.getMemberId().equalsIgnoreCase(memberId))
                .sorted(Comparator.comparing(Rental::getIssueDate).reversed()).toList();
    }

    public void addBook(Book book) throws IOException {
        if (findBookOrNull(book.getIsbn()) != null) {
            throw new IllegalArgumentException("A book with this ISBN already exists.");
        }
        books.add(book);
        persistOrRollback(() -> books.remove(book));
    }

    public void updateBook(String isbn, Book updated) throws IOException {
        Book current = requireBook(isbn);
        if (!current.getIsbn().equals(updated.getIsbn())) {
            throw new IllegalArgumentException("ISBN cannot be changed.");
        }
        long checkedOut = rentals.stream()
                .filter(rental -> rental.isActive() && rental.getIsbn().equals(current.getIsbn())).count();
        if (updated.getTotalCopies() != current.getAvailableCopies() + checkedOut) {
            throw new IllegalArgumentException("Total copies cannot be less than the number currently borrowed.");
        }
        int index = books.indexOf(current);
        books.set(index, updated);
        persistOrRollback(() -> books.set(index, current));
    }

    public void deleteBook(String isbn) throws IOException {
        Book book = requireBook(isbn);
        if (!book.isActive()) throw new IllegalStateException("This book has already been removed from the catalogue.");
        if (rentals.stream().anyMatch(rental -> rental.isActive() && rental.getIsbn().equals(book.getIsbn()))) {
            throw new IllegalStateException("Return all active rentals for this book before deleting it.");
        }
        book.setActive(false);
        persistOrRollback(() -> book.setActive(true));
    }

    public void addMember(Member member) throws IOException {
        if (findMemberOrNull(member.getId()) != null) {
            throw new IllegalArgumentException("A member with this ID already exists.");
        }
        members.add(member);
        persistOrRollback(() -> members.remove(member));
    }

    public void updateMember(String memberId, Member updated) throws IOException {
        Member current = requireMember(memberId);
        if (!current.getId().equalsIgnoreCase(updated.getId())) {
            throw new IllegalArgumentException("Member ID cannot be changed.");
        }
        updated.setActive(current.isActive());
        int index = members.indexOf(current);
        members.set(index, updated);
        persistOrRollback(() -> members.set(index, current));
    }

    public void setMemberActive(String memberId, boolean active) throws IOException {
        Member member = requireMember(memberId);
        boolean previous = member.isActive();
        member.setActive(active);
        persistOrRollback(() -> member.setActive(previous));
    }

    public Rental issueBook(String memberId, String isbn) throws IOException {
        Member member = requireMember(memberId);
        Book book = requireBook(isbn);
        LocalDate today = LocalDate.now();

        if (!member.isActive()) throw new IllegalStateException("This member is inactive.");
        if (!book.isActive()) throw new IllegalStateException("This book is no longer in the catalogue.");
        if (book.getAvailableCopies() < 1) throw new IllegalStateException("No copies of this book are available.");

        List<Rental> activeMemberRentals = rentals.stream()
                .filter(rental -> rental.isActive() && rental.getMemberId().equalsIgnoreCase(member.getId())).toList();
        if (activeMemberRentals.size() >= BORROW_LIMIT) {
            throw new IllegalStateException("A member may borrow at most " + BORROW_LIMIT + " books.");
        }
        if (activeMemberRentals.stream().anyMatch(rental -> rental.getDueDate().isBefore(today))) {
            throw new IllegalStateException("This member has an overdue book and cannot borrow.");
        }
        if (rentals.stream().anyMatch(rental -> rental.getMemberId().equalsIgnoreCase(member.getId())
                && rental.getFineAmount() > 0 && !rental.isFinePaid())) {
            throw new IllegalStateException("This member has an unpaid fine and cannot borrow.");
        }

        Rental rental = new Rental(UUID.randomUUID().toString(), book.getIsbn(), member.getId(),
                today, today.plusDays(RENTAL_DAYS), null, 0, true, false);
        int previousAvailable = book.getAvailableCopies();
        book.setAvailableCopies(previousAvailable - 1);
        rentals.add(rental);
        persistOrRollback(() -> {
            rentals.remove(rental);
            book.setAvailableCopies(previousAvailable);
        });
        return rental;
    }

    public double returnBook(String rentalId) throws IOException {
        Rental rental = rentals.stream().filter(item -> item.getRentalId().equals(rentalId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Rental was not found."));
        if (!rental.isActive()) throw new IllegalStateException("This book has already been returned.");
        Book book = requireBook(rental.getIsbn());
        LocalDate today = LocalDate.now();
        long lateDays = Math.max(0, ChronoUnit.DAYS.between(rental.getDueDate(), today));
        double fine = Math.min(lateDays * FINE_PER_DAY, book.getPrice());
        int previousAvailable = book.getAvailableCopies();
        book.setAvailableCopies(previousAvailable + 1);
        rental.close(today, fine);
        persistOrRollback(() -> {
            rental.reopenForRollback();
            book.setAvailableCopies(previousAvailable);
        });
        return fine;
    }

    public void renewRental(String rentalId) throws IOException {
        Rental rental = requireActiveRental(rentalId);
        if (rental.isRenewed()) throw new IllegalStateException("This rental has already been renewed.");
        if (LocalDate.now().isAfter(rental.getDueDate())) {
            throw new IllegalStateException("An overdue rental cannot be renewed.");
        }
        LocalDate oldDue = rental.getDueDate();
        rental.renew(oldDue.plusDays(RENEWAL_DAYS));
        persistOrRollback(() -> rental.restoreDueDate(oldDue));
    }

    public void payFine(String rentalId) throws IOException {
        Rental rental = rentals.stream().filter(item -> item.getRentalId().equals(rentalId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Rental was not found."));
        boolean previous = rental.isFinePaid();
        rental.markFinePaid();
        persistOrRollback(() -> rental.setFinePaidForRollback(previous));
    }

    public long getTotalBookCopies() {
        return books.stream().filter(Book::isActive).mapToLong(Book::getTotalCopies).sum();
    }

    public long getIssuedCount() {
        return rentals.stream().filter(Rental::isActive).count();
    }

    public long getOverdueCount() {
        return getOverdueRentals().size();
    }

    public long getActiveMemberCount() {
        return members.stream().filter(Member::isActive).count();
    }

    public Book findBook(String isbn) {
        return requireBook(isbn);
    }

    public Member findMember(String memberId) {
        return requireMember(memberId);
    }

    private Rental requireActiveRental(String rentalId) {
        Rental rental = rentals.stream().filter(item -> item.getRentalId().equals(rentalId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Rental was not found."));
        if (!rental.isActive()) throw new IllegalStateException("This rental is no longer active.");
        return rental;
    }

    private Book requireBook(String isbn) {
        Book book = findBookOrNull(isbn);
        if (book == null) throw new IllegalArgumentException("Book was not found.");
        return book;
    }

    private Book findBookOrNull(String isbn) {
        return books.stream().filter(book -> book.getIsbn().equals(isbn)).findFirst().orElse(null);
    }

    private Member requireMember(String memberId) {
        Member member = findMemberOrNull(memberId);
        if (member == null) throw new IllegalArgumentException("Member was not found.");
        return member;
    }

    private Member findMemberOrNull(String memberId) {
        return members.stream().filter(member -> member.getId().equalsIgnoreCase(memberId))
                .findFirst().orElse(null);
    }

    private void validateLoadedData() throws IOException {
        for (Rental rental : rentals) {
            if (findBookOrNull(rental.getIsbn()) == null || findMemberOrNull(rental.getMemberId()) == null) {
                throw new IOException("Saved rental " + rental.getRentalId() + " references a missing book or member.");
            }
        }
        for (Book book : books) {
            long checkedOut = rentals.stream()
                    .filter(rental -> rental.isActive() && rental.getIsbn().equals(book.getIsbn())).count();
            if (book.getTotalCopies() != book.getAvailableCopies() + checkedOut) {
                throw new IOException("Saved copy counts do not match active rentals for ISBN " + book.getIsbn() + ".");
            }
        }
        if (books.stream().map(Book::getIsbn).distinct().count() != books.size()
                || members.stream().map(member -> member.getId().toLowerCase()).distinct().count() != members.size()) {
            throw new IOException("Saved data contains duplicate book ISBNs or member IDs.");
        }
    }

    private void persistOrRollback(Runnable rollback) throws IOException {
        try {
            store.save(snapshot());
        } catch (IOException ex) {
            rollback.run();
            throw ex;
        }
    }

    private Data snapshot() {
        return new Data(books, members, rentals, librarians);
    }

    private static void validatePassword(char[] password) {
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required.");
        return value.trim();
    }
}
