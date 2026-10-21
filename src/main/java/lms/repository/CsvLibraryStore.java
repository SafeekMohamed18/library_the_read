package lms.repository;

import lms.model.Book;
import lms.model.Librarian;
import lms.model.Rental;
import lms.model.Member;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CsvLibraryStore {
    private static final String BOOKS_HEADER =
            "isbn,title,author,category,publisher,year,price,totalCopies,availableCopies,active";
    private static final String MEMBERS_HEADER =
            "id,name,phone,email,address,type,joinDate,active";
    private static final String RENTALS_HEADER =
            "rentalId,isbn,memberId,issueDate,dueDate,returnDate,fineAmount,finePaid,renewed";
    private static final String LEGACY_LOANS_HEADER =
            "loanId,isbn,memberId,issueDate,dueDate,returnDate,fineAmount,finePaid,renewed";
    private static final String LIBRARIANS_HEADER =
            "username,name,phone,email,passwordHash";

    private final Path directory;

    public CsvLibraryStore(Path directory) throws IOException {
        this.directory = directory;
        Files.createDirectories(directory);
    }

    public Data load() throws IOException {
        return new Data(
                readBooks(directory.resolve("books.csv")),
                readMembers(directory.resolve("members.csv")),
                readRentals(),
                readLibrarians(directory.resolve("librarians.csv")));
    }

    public void save(Data data) throws IOException {
        writeCsv("books.csv", BOOKS_HEADER, bookRows(data.books()));
        writeCsv("members.csv", MEMBERS_HEADER, memberRows(data.members()));
        writeCsv("rentals.csv", RENTALS_HEADER, rentalRows(data.rentals()));
        writeCsv("librarians.csv", LIBRARIANS_HEADER, librarianRows(data.librarians()));
    }

    private void writeCsv(String name, String header, List<List<String>> rows) throws IOException {
        StringBuilder content = new StringBuilder(header).append(System.lineSeparator());
        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) content.append(',');
                content.append(escape(row.get(i)));
            }
            content.append(System.lineSeparator());
        }

        Path destination = directory.resolve(name);
        Path temporary = Files.createTempFile(directory, name, ".tmp");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            if (Files.exists(destination)) {
                Files.copy(destination, directory.resolve(name + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private List<List<String>> readRows(Path path, String expectedHeader) throws IOException {
        if (!Files.exists(path)) return List.of();
        List<List<String>> rows = parseCsv(Files.readString(path, StandardCharsets.UTF_8));
        if (rows.isEmpty() || !String.join(",", rows.get(0)).equals(expectedHeader)) {
            throw new IOException("Unexpected or missing CSV header in " + path.getFileName() + ".");
        }
        return rows.subList(1, rows.size());
    }

    private List<Book> readBooks(Path path) throws IOException {
        List<Book> books = new ArrayList<>();
        for (List<String> row : readRows(path, BOOKS_HEADER)) {
            requireColumns(row, 10, path);
            try {
                books.add(new Book(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4),
                        Integer.parseInt(row.get(5)), Double.parseDouble(row.get(6)),
                        Integer.parseInt(row.get(7)), Integer.parseInt(row.get(8)),
                        Boolean.parseBoolean(row.get(9))));
            } catch (RuntimeException ex) {
                throw invalidRow(path, ex);
            }
        }
        return books;
    }

    private List<Member> readMembers(Path path) throws IOException {
        List<Member> members = new ArrayList<>();
        for (List<String> row : readRows(path, MEMBERS_HEADER)) {
            requireColumns(row, 8, path);
            try {
                members.add(new Member(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5),
                        LocalDate.parse(row.get(6)), Boolean.parseBoolean(row.get(7))));
            } catch (RuntimeException ex) {
                throw invalidRow(path, ex);
            }
        }
        return members;
    }

    private List<Rental> readRentals() throws IOException {
        Path path = directory.resolve("rentals.csv");
        String header = RENTALS_HEADER;
        if (!Files.exists(path)) {
            path = directory.resolve("loans.csv");
            header = LEGACY_LOANS_HEADER;
        }
        List<Rental> rentals = new ArrayList<>();
        for (List<String> row : readRows(path, header)) {
            requireColumns(row, 9, path);
            try {
                rentals.add(new Rental(row.get(0), row.get(1), row.get(2), LocalDate.parse(row.get(3)),
                        LocalDate.parse(row.get(4)), row.get(5).isBlank() ? null : LocalDate.parse(row.get(5)),
                        Double.parseDouble(row.get(6)), Boolean.parseBoolean(row.get(7)),
                        Boolean.parseBoolean(row.get(8))));
            } catch (RuntimeException ex) {
                throw invalidRow(path, ex);
            }
        }
        return rentals;
    }

    private List<Librarian> readLibrarians(Path path) throws IOException {
        List<Librarian> librarians = new ArrayList<>();
        for (List<String> row : readRows(path, LIBRARIANS_HEADER)) {
            requireColumns(row, 5, path);
            try {
                librarians.add(new Librarian(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4)));
            } catch (RuntimeException ex) {
                throw invalidRow(path, ex);
            }
        }
        return librarians;
    }

    private List<List<String>> bookRows(List<Book> books) {
        return books.stream().map(book -> List.of(book.getIsbn(), book.getTitle(), book.getAuthor(),
                book.getCategory(), book.getPublisher(), Integer.toString(book.getYear()),
                String.format(Locale.ROOT, "%.2f", book.getPrice()), Integer.toString(book.getTotalCopies()),
                Integer.toString(book.getAvailableCopies()), Boolean.toString(book.isActive()))).toList();
    }

    private List<List<String>> memberRows(List<Member> members) {
        return members.stream().map(member -> List.of(member.getId(), member.getName(), member.getPhone(),
                member.getEmail(), member.getAddress(), member.getMemberType(), member.getJoinDate().toString(),
                Boolean.toString(member.isActive()))).toList();
    }

    private List<List<String>> rentalRows(List<Rental> rentals) {
        return rentals.stream().map(rental -> List.of(rental.getRentalId(), rental.getIsbn(), rental.getMemberId(),
                rental.getIssueDate().toString(), rental.getDueDate().toString(),
                rental.getReturnDate() == null ? "" : rental.getReturnDate().toString(),
                String.format(Locale.ROOT, "%.2f", rental.getFineAmount()),
                Boolean.toString(rental.isFinePaid()), Boolean.toString(rental.isRenewed()))).toList();
    }

    private List<List<String>> librarianRows(List<Librarian> librarians) {
        return librarians.stream().map(user -> List.of(user.getUsername(), user.getName(), user.getPhone(),
                user.getEmail(), user.getPasswordHash())).toList();
    }

    private static String escape(String value) {
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0
                && value.indexOf('\n') < 0 && value.indexOf('\r') < 0) return value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static List<List<String>> parseCsv(String text) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (quoted) {
                if (ch == '"' && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (ch == '"') {
                    quoted = false;
                } else {
                    field.append(ch);
                }
            } else if (ch == '"' && field.length() == 0) {
                quoted = true;
            } else if (ch == ',') {
                row.add(field.toString());
                field.setLength(0);
            } else if (ch == '\n' || ch == '\r') {
                if (ch == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
                row.add(field.toString());
                field.setLength(0);
                if (!(row.size() == 1 && row.get(0).isEmpty())) rows.add(row);
                row = new ArrayList<>();
            } else {
                field.append(ch);
            }
        }
        if (quoted) throw new IOException("CSV file contains an unclosed quoted field.");
        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }

    private static void requireColumns(List<String> row, int expected, Path path) throws IOException {
        if (row.size() != expected) {
            throw new IOException("Invalid row in " + path.getFileName() + ": expected "
                    + expected + " columns but found " + row.size() + ".");
        }
    }

    private static IOException invalidRow(Path path, RuntimeException cause) {
        return new IOException("Invalid data in " + path.getFileName() + ": " + cause.getMessage(), cause);
    }

    public record Data(List<Book> books, List<Member> members, List<Rental> rentals,
                       List<Librarian> librarians) {
        public Data {
            books = List.copyOf(books);
            members = List.copyOf(members);
            rentals = List.copyOf(rentals);
            librarians = List.copyOf(librarians);
        }
    }
}
