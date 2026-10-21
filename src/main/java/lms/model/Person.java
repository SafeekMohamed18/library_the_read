package lms.model;

public abstract class Person {
    private final String id;
    private String name;
    private String phone;
    private String email;

    protected Person(String id, String name, String phone, String email) {
        this.id = requireText(id, "ID");
        setName(name);
        setPhone(phone);
        setEmail(email);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "Name");
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        String value = requireText(phone, "Phone");
        if (!value.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone must contain exactly 10 digits.");
        }
        this.phone = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        String value = requireText(email, "Email");
        if (!value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        this.email = value;
    }

    public abstract String getRole();

    protected static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }
}
