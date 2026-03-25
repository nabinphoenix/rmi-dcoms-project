package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import database.DBManager;
import model.Employee;
import model.FamilyDetail;
import model.LeaveApplication;
import model.ChatMessage;
import remote.HRMService;

/**
 * RMI Service Implementation.
 * Extends UnicastRemoteObject to make this class exportable as a remote object.
 * Implements all methods defined in the HRMService remote interface.
 * All methods that access the database are synchronized for thread safety,
 * since multiple RMI clients may call these methods concurrently.
 */
public class HRMServiceImpl extends UnicastRemoteObject implements HRMService {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(HRMServiceImpl.class.getName());

    /**
     * Constructor must throw RemoteException as required by UnicastRemoteObject.
     * When this object is constructed, it is automatically exported to listen
     * for incoming RMI calls on an anonymous port.
     */
    public HRMServiceImpl() throws RemoteException {
        super(); // Export this remote object with an anonymous port
    }

    /**
     * Hashes a plain text password using SHA-256 algorithm.
     * Returns a hex string representation of the hash.
     * 
     * @param password the plain text password
     * @return SHA-256 hashed password as hex string
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Authenticates an employee by hashing the provided password
     * and comparing against the stored hash in the database.
     */
    @Override
    public synchronized Employee login(String username, String password) throws RemoteException {
        System.out.println("[SERVER] Login request received for: " + username);
        String hashedPassword = hashPassword(password);
        System.out.println("[SERVER] Generated Hash for comparison: " + hashedPassword);
        Employee emp = DBManager.loginEmployee(username, hashedPassword);
        if (emp != null) {
            System.out.println("[SERVER] Login SUCCESS for: " + emp.getUsername() + " (" + emp.getRole() + ")");
        } else {
            System.out.println("[SERVER] Login FAILED for: " + username);
        }
        return emp;
    }

    /**
     * Registers a new employee.
     * Auto-generates username from first name + last name (lowercase, no spaces).
     * If the username already exists, appends a number suffix to make it unique.
     * Default password is set to "password123" and hashed with SHA-256.
     * A leave balance record (14 annual, 14 sick) is also created.
     */
    @Override
    public synchronized boolean registerEmployee(Employee emp) throws RemoteException {
        // Auto-generate username: firstname.lastname (lowercase)
        String baseUsername = (emp.getFirstName().toLowerCase() + "." + emp.getLastName().toLowerCase())
                .replaceAll("\\s+", "");
        String username = baseUsername;
        int counter = 1;
        // Ensure username is unique by appending a number if needed
        while (DBManager.usernameExists(username)) {
            username = baseUsername + counter;
            counter++;
        }
        emp.setUsername(username);

        // Set default password and hash it with SHA-256
        String defaultPassword = "password123";
        emp.setPassword(hashPassword(defaultPassword));

        // Default role for new registrations is EMPLOYEE
        emp.setRole("EMPLOYEE");

        boolean result = DBManager.registerEmployee(emp);
        if (result) {
            System.out.println("[SERVER] New employee registered: " + emp.getUsername());
        } else {
            System.out.println("[SERVER] Failed to register employee: " + emp.getFirstName() + " " + emp.getLastName());
        }
        return result;
    }

    /**
     * Updates an employee's profile (first name and last name).
     */
    @Override
    public synchronized boolean updateProfile(Employee emp) throws RemoteException {
        boolean result = DBManager.updateProfile(emp);
        if (result) {
            System.out.println("[SERVER] Profile updated for employee ID: " + emp.getId());
        }
        return result;
    }

    /**
     * Updates family details for an employee.
     * Replaces all existing family records with the new list (delete + insert).
     */
    @Override
    public synchronized boolean updateFamilyDetails(int employeeId, List<FamilyDetail> details) throws RemoteException {
        boolean result = DBManager.updateFamilyDetails(employeeId, details);
        if (result) {
            System.out.println("[SERVER] Family details updated for employee ID: " + employeeId);
        }
        return result;
    }

    /**
     * Checks the leave balance for a specific leave type.
     * Returns the number of remaining days.
     */
    @Override
    public synchronized int checkLeaveBalance(int employeeId, String leaveType) throws RemoteException {
        return DBManager.checkLeaveBalance(employeeId, leaveType);
    }

    /**
     * Applies for leave. Validates sufficient balance before approving.
     * Calculates the number of days between start and end date,
     * checks the balance, deducts if sufficient, and records the application.
     */
    @Override
    public synchronized boolean applyLeave(LeaveApplication application) throws RemoteException {
        // Calculate the number of leave days (inclusive of start and end date)
        LocalDate start = LocalDate.parse(application.getStartDate());
        LocalDate end = LocalDate.parse(application.getEndDate());
        long days = ChronoUnit.DAYS.between(start, end) + 1; // +1 to include both start and end

        if (days <= 0) {
            System.out.println("[SERVER] Invalid leave dates for employee ID: " + application.getEmployeeId());
            return false;
        }

        // Check if employee has sufficient leave balance (Preliminary check)
        int currentBalance = DBManager.checkLeaveBalance(application.getEmployeeId(), application.getLeaveType());
        if (currentBalance < days) {
            System.out.println("[SERVER] Insufficient leave balance for employee ID: " + application.getEmployeeId()
                    + " (Requested: " + days + ", Available: " + currentBalance + ")");
            return false;
        }

        // Record the leave application without deducting balance immediately
        boolean result = DBManager.applyLeave(application);
        if (result) {
            System.out.println("[SERVER] Leave request recorded for employee ID: " + application.getEmployeeId()
                    + " (" + application.getLeaveType() + ": " + days + " days - PENDING)");
        }
        return result;
    }

    /**
     * Retrieves all leave applications for a given employee.
     */
    @Override
    public synchronized List<LeaveApplication> checkLeaveStatus(int employeeId) throws RemoteException {
        return DBManager.getLeaveApplications(employeeId);
    }

    /**
     * Generates a comprehensive yearly report for an employee.
     * Includes: profile info, family details, and leave history for the specified
     * year.
     * Returns a formatted string for display in a popup dialog.
     */
    @Override
    public synchronized String generateYearlyReport(int employeeId, int year) throws RemoteException {
        Employee emp = DBManager.getEmployeeById(employeeId);
        if (emp == null) {
            return "ERROR: Employee not found with ID: " + employeeId;
        }

        List<FamilyDetail> family = DBManager.getFamilyDetails(employeeId);
        List<LeaveApplication> leaves = DBManager.getLeaveApplicationsByYear(employeeId, year);
        int annualLeft = DBManager.checkLeaveBalance(employeeId, "Annual");
        int sickLeft = DBManager.checkLeaveBalance(employeeId, "Sick");

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================\n");
        sb.append("           YEARLY EMPLOYEE REPORT - ").append(year).append("\n");
        sb.append("==========================================================\n\n");

        // Profile Section
        sb.append("--- EMPLOYEE PROFILE ---\n");
        sb.append("Full Name    : ").append(emp.getFirstName()).append(" ").append(emp.getLastName()).append("\n");
        sb.append("IC/Passport  : ").append(emp.getIcPassport()).append("\n");
        sb.append("Username     : ").append(emp.getUsername()).append("\n");
        sb.append("Role         : ").append(emp.getRole()).append("\n\n");

        // Family Details Section
        sb.append("--- FAMILY DETAILS ---\n");
        if (family.isEmpty()) {
            sb.append("No family details on record.\n");
        } else {
            sb.append(String.format("%-20s %-15s %-15s\n", "Name", "Relationship", "IC/Passport"));
            sb.append("----------------------------------------------------------\n");
            for (FamilyDetail f : family) {
                sb.append(String.format("%-20s %-15s %-15s\n", f.getName(), f.getRelationship(), f.getIcPassport()));
            }
        }
        sb.append("\n");

        // Leave History Section
        sb.append("--- LEAVE HISTORY (").append(year).append(") ---\n");
        if (leaves.isEmpty()) {
            sb.append("No leave applications found for this year.\n");
        } else {
            sb.append(String.format("%-10s %-12s %-12s %-10s\n", "Type", "Start", "End", "Status"));
            sb.append("----------------------------------------------------------\n");
            for (LeaveApplication l : leaves) {
                sb.append(String.format("%-10s %-12s %-12s %-10s\n",
                        l.getLeaveType(), l.getStartDate(), l.getEndDate(), l.getStatus()));
            }
        }
        sb.append("\n");

        // Leave Balance Section
        sb.append("--- REMAINING LEAVE BALANCE ---\n");
        sb.append("Annual Leave : ").append(annualLeft).append(" days\n");
        sb.append("Sick Leave   : ").append(sickLeft).append(" days\n");

        sb.append("\n==========================================================\n");
        sb.append("                END OF REPORT\n");
        sb.append("==========================================================\n");

        return sb.toString();
    }

    /**
     * Retrieves all pending leave applications for HR approval.
     */
    @Override
    public synchronized List<LeaveApplication> getPendingLeaveApplications() throws RemoteException {
        try {
            return DBManager.getAllPendingLeaveApplications();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Database error in getPendingLeaveApplications", e);
            throw new RemoteException("Database temporarily unavailable.");
        }
    }

    /**
     * Approves a leave application and triggers payroll update.
     */
    @Override
    public synchronized boolean approveLeave(int leaveId) throws RemoteException {
        try {
            LeaveApplication app = DBManager.getLeaveApplicationById(leaveId);
            if (app == null) return false;

            // Calculate days to deduct
            LocalDate start = LocalDate.parse(app.getStartDate());
            LocalDate end = LocalDate.parse(app.getEndDate());
            int days = (int) (ChronoUnit.DAYS.between(start, end) + 1);

            // Re-check balance before final approval
            int balance = DBManager.checkLeaveBalance(app.getEmployeeId(), app.getLeaveType());
            if (balance < days) {
                logger.warning("[SERVER] Cannot approve leave ID " + leaveId + ": Insufficient balance now!");
                return false;
            }

            // DEDUCT BALANCE HERE
            boolean deducted = DBManager.deductLeaveBalance(app.getEmployeeId(), app.getLeaveType(), days);
            if (!deducted) return false;

            boolean result = DBManager.updateLeaveStatus(leaveId, "APPROVED");
            if (result) {
                logger.info("[SERVER] HR Approved leave application ID: " + leaveId + " (Deducted " + days + " days)");
                // Simulate payroll update
                updatePayrollAfterLeaveApproval(app.getEmployeeId(), days);
            } else {
                // Rollback deduction if status update fails (basic consistency)
                DBManager.deductLeaveBalance(app.getEmployeeId(), app.getLeaveType(), -days);
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in approveLeave: " + leaveId, e);
            throw new RemoteException("Failed to approve leave due to server error.");
        }
    }

    /**
     * Rejects a leave application.
     */
    @Override
    public synchronized boolean rejectLeave(int leaveId) throws RemoteException {
        try {
            boolean result = DBManager.updateLeaveStatus(leaveId, "REJECTED");
            if (result) {
                logger.info("[SERVER] HR Rejected leave application ID: " + leaveId);
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in rejectLeave: " + leaveId, e);
            throw new RemoteException("Failed to reject leave due to server error.");
        }
    }

    /**
     * Simulation of communication with Payroll System.
     */
    @Override
    public synchronized boolean updatePayrollAfterLeaveApproval(int employeeId, int leaveDays) throws RemoteException {
        try {
            PayrollService.updatePayroll(employeeId, leaveDays);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in updatePayrollAfterLeaveApproval", e);
            return false;
        }
    }

    /**
     * Retrieves an employee's profile.
     */
    @Override
    public synchronized Employee getEmployeeProfile(int employeeId) throws RemoteException {
        try {
            Employee emp = DBManager.getEmployeeById(employeeId);
            if (emp == null) {
                logger.warning("Profile requested for non-existent employee ID: " + employeeId);
            }
            return emp;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getEmployeeProfile", e);
            throw new RemoteException("Database error while fetching profile.");
        }
    }

    /**
     * Retrieves statistics for the HR dashboard.
     */
    @Override
    public synchronized Map<String, Integer> getHRStatistics() throws RemoteException {
        try {
            Map<String, Integer> stats = new HashMap<>();
            List<Employee> allEmps = DBManager.getAllEmployees();
            List<LeaveApplication> allPending = DBManager.getAllPendingLeaveApplications();

            stats.put("TotalEmployees", allEmps.size());
            stats.put("PendingLeaveRequests", allPending.size());

            // Simple count for approved leaves (this year)
            int approvedCount = 0;
            for (Employee e : allEmps) {
                List<LeaveApplication> history = DBManager.getLeaveApplications(e.getId());
                for (LeaveApplication l : history) {
                    if ("APPROVED".equals(l.getStatus())) {
                        approvedCount++;
                    }
                }
            }
            stats.put("ApprovedLeaves", approvedCount);
            stats.put("TotalLeaveRequests", approvedCount + allPending.size());

            return stats;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error generating HR statistics", e);
            return new HashMap<>();
        }
    }

    @Override
    public synchronized List<FamilyDetail> getFamilyDetails(int employeeId) throws RemoteException {
        return DBManager.getFamilyDetails(employeeId);
    }

    @Override
    public synchronized List<Employee> getAllEmployees() throws RemoteException {
        return DBManager.getAllEmployees();
    }

    @Override
    public synchronized boolean updateLeaveBalance(int empId, int annual, int sick) throws RemoteException {
        return DBManager.updateLeaveBalance(empId, annual, sick);
    }

    @Override
    public synchronized List<ChatMessage> getChatHistory(String u1, String u2) throws RemoteException {
        return DBManager.getChatHistory(u1, u2);
    }

    @Override
    public synchronized String getHRUsername() throws RemoteException {
        return DBManager.getHRUsername();
    }

    @Override
    public synchronized boolean deleteEmployee(int employeeId) throws RemoteException {
        try {
            boolean result = DBManager.softDeleteEmployee(employeeId);
            if (result) {
                logger.info("[SERVER] Employee soft-deleted: ID " + employeeId);
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in deleteEmployee", e);
            throw new RemoteException("Failed to delete employee.");
        }
    }

    @Override
    public synchronized boolean updateEmployeeProfile(Employee emp) throws RemoteException {
        try {
            if (emp.getPassword() != null && !emp.getPassword().isEmpty()) {
                emp.setPassword(hashPassword(emp.getPassword()));
            }
            boolean result = DBManager.updateEmployeeProfile(emp);
            if (result) {
                logger.info("[SERVER] Full profile update for Employee ID " + emp.getId());
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in updateEmployeeProfile", e);
            throw new RemoteException("Failed to update employee profile.");
        }
    }

    @Override
    public synchronized boolean icPassportExists(String ic) throws RemoteException {
        return DBManager.icPassportExists(ic);
    }

    @Override
    public synchronized boolean changePassword(int id, String oldPass, String newPass) throws RemoteException {
        try {
            String hashedOld = hashPassword(oldPass);
            String hashedNew = hashPassword(newPass);
            boolean success = DBManager.changePassword(id, hashedOld, hashedNew);
            if (success) {
                logger.info("[SERVER] Password changed successfully for Employee ID " + id);
            }
            return success;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in changePassword", e);
            throw new RemoteException("An error occurred while changing password.");
        }
    }

    @Override
    public synchronized List<LeaveApplication> getAllLeaveApplications() throws RemoteException {
        try {
            return DBManager.getAllLeaveApplications();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in getAllLeaveApplications", e);
            throw new RemoteException("Database error while fetching leave history.");
        }
    }
}
