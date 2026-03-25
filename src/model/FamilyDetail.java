package model;

import java.io.Serializable;

/**
 * FamilyDetail model class.
 * Implements Serializable so it can be transferred over RMI.
 */
public class FamilyDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int employeeId;
    private String name;
    private String relationship;
    private String icPassport;

    // Default constructor
    public FamilyDetail() {
    }

    // Constructor without id (for adding new family detail)
    public FamilyDetail(int employeeId, String name, String relationship, String icPassport) {
        this.employeeId = employeeId;
        this.name = name;
        this.relationship = relationship;
        this.icPassport = icPassport;
    }

    // Full constructor (for fetching from database)
    public FamilyDetail(int id, int employeeId, String name, String relationship, String icPassport) {
        this.id = id;
        this.employeeId = employeeId;
        this.name = name;
        this.relationship = relationship;
        this.icPassport = icPassport;
    }

    // ----- Getters -----

    public int getId() {
        return id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getName() {
        return name;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getIcPassport() {
        return icPassport;
    }

    // ----- Setters -----

    public void setId(int id) {
        this.id = id;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public void setIcPassport(String icPassport) {
        this.icPassport = icPassport;
    }

    @Override
    public String toString() {
        return "FamilyDetail [id=" + id + ", employeeId=" + employeeId + ", name=" + name
                + ", relationship=" + relationship + ", icPassport=" + icPassport + "]";
    }
}
