package lms.model;

public final class Librarian extends Person {
    private final String username;
    private final String passwordHash;

    public Librarian(String username, String fullName, String phone, String email, String passwordHash) {
        super(username, fullName, phone, email);
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public String getRole() {
        return "Librarian";
    }
}
