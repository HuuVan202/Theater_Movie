package movie_theater_gr4.project_gr4.enums;

public enum Roles {
    ADMIN_SUPPORT(4),
    ADMIN(1),
    EMPLOYEE(2),
    MEMBER(3);

    private final int value;

    // Constructor
    Roles(int value) {
        this.value = value;
    }

    // Getter for the value
    public int getValue() {
        return value;
    }

    public static Roles fromValue(int value) {
        for (Roles role : Roles.values()) {
            if (role.value == value) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role value: " + value);
    }
}
