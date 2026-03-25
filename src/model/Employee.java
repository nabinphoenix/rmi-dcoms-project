package model;

import java.io.Serializable;

/**
 * Employee model class.
 * Implements Serializable so it can be sent over RMI network calls.
 */
public class Employee implements Serializable {

    // Serial version UID for consistent serialization across JVMs
    private static final long serialVersionUID = 1L;

    private int id;
    private String firstName;
    private String lastName;
    private String icPassport;
    private String username;
    private String password;
    private String role; // "HR" or "EMPLOYEE"
    private int isActive = 1; // 1 for Active, 0 for Deleted

    // Default constructor
    public Employee() {
    }

    // Constructor without id (for new employee registration)
    public Employee(String firstName, String lastName, String icPassport,
                    String username, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassport = icPassport;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Full constructor (for fetching from database)
    public Employee(int id, String firstName, String lastName, String icPassport,
                    String username, String password, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.icPassport = icPassport;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ----- Getters -----

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getIcPassport() {
        return icPassport;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    // ----- Setters -----

    public void setId(int id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setIcPassport(String icPassport) {
        this.icPassport = icPassport;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public String getStatusString() {
        return isActive == 1 ? "ACTIVE" : "INACTIVE";
    }

    @Override
    public String toString() {
        return "Employee [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName
                + ", icPassport=" + icPassport + ", username=" + username + ", role=" + role + ", status=" + getStatusString() + "]";
    }
}
