package lms.model;

public final class Member {
    private final String memberId;
    private String name;
    private String contactNumber;

    public Member(String memberId, String name, String contactNumber) {
        this.memberId = requireText(memberId, "Member ID");
        setName(name);
        setContactNumber(contactNumber);
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "Member name");
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = requireText(contactNumber, "Contact number");
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }
}
