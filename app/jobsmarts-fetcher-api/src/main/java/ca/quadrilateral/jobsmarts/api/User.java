package ca.quadrilateral.jobsmarts.api;

public class User {
    private final String name;
    private final String email;
    
    private User(final String name, final String email) {
        this.name = name;
        this.email = email;
    }
    
    public static User of(final String name, final String email) {
        return new User(name, email);
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
}
