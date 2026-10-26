package lms;

import lms.model.Book;
import lms.model.Member;

public final class SampleData {
    private SampleData() {
    }

    public static void populate(Library library) {
        library.addBook(new Book("1234560007895", "Kovai", "Keerthi", "Comedy"));
        library.addBook(new Book("9780000000001", "The Midnight Library", "Matt Haig", "Fiction"));
        library.addBook(new Book("9780000000002", "Wings of Fire", "A. P. J. Abdul Kalam", "Autobiography"));
        library.addBook(new Book("9780000000003", "The Alchemist", "Paulo Coelho", "Fiction"));
        library.addBook(new Book("9780000000004", "Atomic Habits", "James Clear", "Self-help"));
        library.addBook(new Book("9780000000005", "Pride and Prejudice", "Jane Austen", "Classic"));
        library.addBook(new Book("9780000000006", "The Blue Umbrella", "Ruskin Bond", "Children"));
        library.addBook(new Book("9780000000007", "Ikigai", "Hector Garcia", "Self-help"));

        library.addMember(new Member("M001", "Kuga", "0123456789"));
        library.addMember(new Member("M002", "Ananya Perera", "7000000002"));
        library.addMember(new Member("M003", "Ravi Kumar", "7000000003"));
        library.addMember(new Member("M004", "Nila Fernando", "7000000004"));
        library.addMember(new Member("M005", "Arjun Silva", "7000000005"));
        library.addMember(new Member("M006", "Maya Sen", "7000000006"));
        library.addMember(new Member("M007", "Ishan Patel", "7000000007"));
        library.addMember(new Member("M008", "Leela Jayasuriya", "7000000008"));
        library.addMember(new Member("M009", "Dev Fernando", "7000000009"));
        library.addMember(new Member("M010", "Sara Nair", "7000000010"));
        library.addMember(new Member("M011", "Noah Peris", "7000000011"));
    }
}
