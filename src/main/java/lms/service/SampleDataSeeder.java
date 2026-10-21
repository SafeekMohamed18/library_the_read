package lms.service;

import lms.model.Book;
import lms.model.Member;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class SampleDataSeeder {
    private static final String MARKER_FILE = ".sample-data-seeded";

    private SampleDataSeeder() { }

    public static void seedIfNeeded(LibraryService service, Path dataDirectory) throws IOException {
        Path marker = dataDirectory.resolve(MARKER_FILE);
        if (Files.exists(marker)) return;

        for (Book book : sampleBooks()) {
            if (!service.containsBook(book.getIsbn())) service.addBook(book);
        }
        for (Member member : sampleMembers()) {
            if (!service.containsMember(member.getId())) service.addMember(member);
        }

        Files.writeString(marker, "Sample data initialized." + System.lineSeparator());
    }

    private static List<Book> sampleBooks() {
        return List.of(
                new Book("9780000000001", "The Midnight Library", "Matt Haig", "Fiction",
                        "Canongate", 2020, 450.00, 4, 4),
                new Book("9780000000002", "Wings of Fire", "A. P. J. Abdul Kalam", "Autobiography",
                        "Universities Press", 1999, 350.00, 5, 5),
                new Book("9780000000003", "The Alchemist", "Paulo Coelho", "Fiction",
                        "HarperOne", 1988, 299.00, 3, 3),
                new Book("9780000000004", "Atomic Habits", "James Clear", "Self-help",
                        "Avery", 2018, 599.00, 4, 4),
                new Book("9780000000005", "Pride and Prejudice", "Jane Austen", "Classic",
                        "T. Egerton", 1813, 250.00, 2, 2),
                new Book("9780000000006", "The Blue Umbrella", "Ruskin Bond", "Children",
                        "Rupa Publications", 1980, 180.00, 3, 3),
                new Book("9780000000007", "Ikigai", "Hector Garcia", "Self-help",
                        "Penguin Books", 2016, 399.00, 4, 4));
    }

    private static List<Member> sampleMembers() {
        LocalDate joined = LocalDate.now();
        return List.of(
                member("M002", "Ananya Perera", "7000000002", "ananya@example.com", "12 Lake Road", "Student", joined),
                member("M003", "Ravi Kumar", "7000000003", "ravi@example.com", "8 Station Road", "Student", joined),
                member("M004", "Nila Fernando", "7000000004", "nila@example.com", "21 Garden Lane", "Staff", joined),
                member("M005", "Arjun Silva", "7000000005", "arjun@example.com", "5 Temple Street", "Student", joined),
                member("M006", "Maya Sen", "7000000006", "maya@example.com", "14 Park Avenue", "Student", joined),
                member("M007", "Ishan Patel", "7000000007", "ishan@example.com", "3 School Road", "Staff", joined),
                member("M008", "Leela Jayasuriya", "7000000008", "leela@example.com", "9 River View", "Student", joined),
                member("M009", "Dev Fernando", "7000000009", "dev@example.com", "17 Hill Street", "Student", joined),
                member("M010", "Sara Nair", "7000000010", "sara@example.com", "6 Market Road", "Staff", joined),
                member("M011", "Noah Peris", "7000000011", "noah@example.com", "11 School Lane", "Student", joined));
    }

    private static Member member(String id, String name, String phone, String email, String address,
                                 String type, LocalDate joinDate) {
        return new Member(id, name, phone, email, address, type, joinDate, true);
    }
}
