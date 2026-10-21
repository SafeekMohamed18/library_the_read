package lms.model;

import java.time.LocalDate;

public final class Member extends Person {
    private final String memberType;
    private final LocalDate joinDate;
    private final String address;
    private boolean active;

    public Member(String id, String name, String phone, String email, String address, String memberType,
                  LocalDate joinDate, boolean active) {
        super(id, name, phone, email);
        if (!"Student".equals(memberType) && !"Staff".equals(memberType)) {
            throw new IllegalArgumentException("Member type must be Student or Staff.");
        }
        this.memberType = memberType;
        this.joinDate = joinDate == null ? LocalDate.now() : joinDate;
        this.address = requireText(address, "Address");
        this.active = active;
    }

    public String getMemberType() {
        return memberType;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public String getAddress() {
        return address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String getRole() {
        return "Member";
    }
}
