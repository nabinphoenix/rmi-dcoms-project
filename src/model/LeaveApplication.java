package model;

import java.io.Serializable;

/**
 * LeaveApplication model class.
 * Implements Serializable so it can be transferred over RMI.
 */
public class LeaveApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int employeeId;
    private String leaveType;   // "Annual" or "Sick"
    private String startDate;   // Date stored as String in yyyy-MM-dd format
    private String endDate;     // Date stored as String in yyyy-MM-dd format
    private String status;      // "PENDING", "APPROVED", "REJECTED"
    private String appliedDate; // Date stored as String in yyyy-MM-dd format

    // Default constructor
    public LeaveApplication() {
    }

    // Constructor without id and appliedDate (for new leave application)
    public LeaveApplication(int employeeId, String leaveType, String startDate, String endDate) {
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "PENDING";
    }

    // Full constructor (for fetching from database)
    public LeaveApplication(int id, int employeeId, String leaveType, String startDate,
                            String endDate, String status, String appliedDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.appliedDate = appliedDate;
    }

    // ----- Getters -----

    public int getId() {
        return id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    // ----- Setters -----

    public void setId(int id) {
        this.id = id;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }

    @Override
    public String toString() {
        return "LeaveApplication [id=" + id + ", employeeId=" + employeeId + ", leaveType=" + leaveType
                + ", startDate=" + startDate + ", endDate=" + endDate + ", status=" + status
                + ", appliedDate=" + appliedDate + "]";
    }
}
