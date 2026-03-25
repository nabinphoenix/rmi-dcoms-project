package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import model.Employee;
import model.FamilyDetail;
import model.LeaveApplication;
import model.ChatMessage;

/**
 * RMI Remote Interface for the HRM System.
 * This interface defines all the operations that clients can invoke on the
 * server.
 * Every method must throw RemoteException as required by Java RMI
 * specification.
 */
public interface HRMService extends Remote {

    /**
     * Authenticates an employee using username and SHA-256 hashed password.
     * 
     * @param username the employee's username
     * @param password the plain text password (will be hashed before comparison)
     * @return Employee object if credentials are valid, null otherwise
     * @throws RemoteException if a network/RMI communication error occurs
     */
    Employee login(String username, String password) throws RemoteException;

    /**
     * Registers a new employee into the system (HR only).
     * Password is hashed with SHA-256 before storage.
     * Also creates a leave balance record with defaults (14 annual, 14 sick).
     * 
     * @param emp the Employee object with registration details
     * @return true if registration succeeds, false otherwise
     * @throws RemoteException if a network/RMI communication error occurs
     */
    boolean registerEmployee(Employee emp) throws RemoteException;

    /**
     * Updates an existing employee's profile (first name, last name).
     * 
     * @param emp the Employee object with updated details
     * @return true if update succeeds, false otherwise
     * @throws RemoteException if a network/RMI communication error occurs
     */
    boolean updateProfile(Employee emp) throws RemoteException;

    /**
     * Adds or updates family details for an employee.
     * Existing family records are deleted and replaced with the new list.
     * 
     * @param employeeId the employee's ID
     * @param details    list of FamilyDetail objects
     * @return true if update succeeds, false otherwise
     * @throws RemoteException if a network/RMI communication error occurs
     */
    boolean updateFamilyDetails(int employeeId, List<FamilyDetail> details) throws RemoteException;

    /**
     * Retrieves the remaining leave balance for an employee.
     * 
     * @param employeeId the employee's ID
     * @param leaveType  the type of leave ("Annual" or "Sick")
     * @return remaining leave days, or -1 if not found
     * @throws RemoteException if a network/RMI communication error occurs
     */
    int checkLeaveBalance(int employeeId, String leaveType) throws RemoteException;

    /**
     * Submits a new leave application for an employee.
     * 
     * @param application the LeaveApplication object with leave details
     * @return true if application succeeds, false otherwise
     * @throws RemoteException if a network/RMI communication error occurs
     */
    boolean applyLeave(LeaveApplication application) throws RemoteException;

    /**
     * Retrieves all leave applications for an employee.
     * 
     * @param employeeId the employee's ID
     * @return list of LeaveApplication objects
     * @throws RemoteException if a network/RMI communication error occurs
     */
    List<LeaveApplication> checkLeaveStatus(int employeeId) throws RemoteException;

    /**
     * Generates a yearly report for a specific employee (HR only).
     * Includes profile, family details, and leave history for the given year.
     * 
     * @param employeeId the employee's ID
     * @param year       the year for the report
     * @return formatted report string
     * @throws RemoteException if a network/RMI communication error occurs
     */
    String generateYearlyReport(int employeeId, int year) throws RemoteException;

    /**
     * Retrieves all pending leave applications for HR approval (Requested method name).
     */
    List<LeaveApplication> getPendingLeaveApplications() throws RemoteException;

    /**
     * Approves a leave application (Requested method name).
     */
    boolean approveLeave(int leaveId) throws RemoteException;

    /**
     * Rejects a leave application (Requested method name).
     */
    boolean rejectLeave(int leaveId) throws RemoteException;

    /**
     * Simulates payroll update after leave approval.
     */
    boolean updatePayrollAfterLeaveApproval(int employeeId, int leaveDays) throws RemoteException;

    /**
     * Retrieves an employee's profile by ID.
     */
    Employee getEmployeeProfile(int employeeId) throws RemoteException;

    /**
     * Retrieves HR dashboard statistics.
     */
    java.util.Map<String, Integer> getHRStatistics() throws RemoteException;

    /**
     * Retrieves family details for an employee.
     */
    List<FamilyDetail> getFamilyDetails(int employeeId) throws RemoteException;

    /**
     * Retrieves all employees with the 'EMPLOYEE' role.
     */
    List<Employee> getAllEmployees() throws RemoteException;

    /**
     * Updates the leave balance for an employee.
     */
    boolean updateLeaveBalance(int empId, int annual, int sick) throws RemoteException;

    /**
     * Retrieves chat history between two users.
     */
    List<ChatMessage> getChatHistory(String u1, String u2) throws RemoteException;

    /**
     * Retrieves the username of the primary HR administrator.
     */
    String getHRUsername() throws RemoteException;

    /**
     * Soft deletes an employee by setting is_active = 0.
     */
    boolean deleteEmployee(int employeeId) throws RemoteException;

    /**
     * Full profile update for an employee (Used by HR).
     */
    boolean updateEmployeeProfile(Employee emp) throws RemoteException;

    /**
     * Checks if an IC/Passport already exists in the system.
     */
    boolean icPassportExists(String ic) throws RemoteException;

    /**
     * Changes an employee's password.
     */
    boolean changePassword(int employeeId, String oldPassword, String newPassword) throws RemoteException;

    /**
     * Retrieves all leave applications (Approved, Rejected, Pending) for HR history.
     */
    List<LeaveApplication> getAllLeaveApplications() throws RemoteException;
}
