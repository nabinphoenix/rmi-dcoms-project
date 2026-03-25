package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Employee;
import model.FamilyDetail;
import model.LeaveApplication;
import model.ChatMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Manager class.
 * Handles all JDBC operations for the HRM System.
 * Fully verified with all 16 required methods.
 */
public class DBManager {

    private static final String URL = "jdbc:mysql://localhost:3306/hrmdb";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final Logger logger = Logger.getLogger(DBManager.class.getName());

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initDatabase();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS payroll_history (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "employee_id INT, " +
                    "action_type VARCHAR(50), " +
                    "days_deducted INT, " +
                    "process_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (employee_id) REFERENCES employees(id))");
        } catch (SQLException e) {
            logger.severe("[DB] Initial setup error: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static Employee loginEmployee(String username, String hashedPassword) {
        String sql = "SELECT * FROM employees WHERE username = ? AND password = ? AND is_active = 1";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                logger.info("[DB] User login successful: " + username);
                // Assuming Employee constructor or setter can handle is_active status
                Employee employee = new Employee(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("ic_passport"), rs.getString("username"), rs.getString("password"),
                        rs.getString("role"));
                employee.setIsActive(rs.getInt("is_active")); // Set the active status
                return employee;
            } else {
                logger.warning("[DB] Invalid credentials or inactive account for: " + username);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[DB ERROR] Failed to authenticate user: " + username, e);
        }
        return null;
    }

    public static boolean registerEmployee(Employee emp) {
        String insertEmpSQL = "INSERT INTO employees (first_name, last_name, ic_passport, username, password, role, is_active) VALUES (?, ?, ?, ?, ?, ?, 1)";
        String insertLeaveBalSQL = "INSERT INTO leave_balance (employee_id, annual_leave, sick_leave) VALUES (?, 14, 14)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(insertEmpSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, emp.getFirstName());
                pstmt.setString(2, emp.getLastName());
                pstmt.setString(3, emp.getIcPassport());
                pstmt.setString(4, emp.getUsername());
                pstmt.setString(5, emp.getPassword());
                pstmt.setString(6, emp.getRole());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int empId = rs.getInt(1);
                    try (PreparedStatement lp = conn.prepareStatement(insertLeaveBalSQL)) {
                        lp.setInt(1, empId);
                        lp.executeUpdate();
                    }
                }
            }
            conn.commit();
            logger.info("[DB] Employee registered successfully: " + emp.getUsername());
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[DB ERROR] Registration failed for: " + emp.getUsername(), e);
            return false;
        }
    }

    // 3. usernameExists(String username)
    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM employees WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 4. getAllEmployees()
    public static List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, ic_passport, username, role, is_active FROM employees";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Employee e = new Employee(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("ic_passport"), rs.getString("username"), "", rs.getString("role"));
                e.setIsActive(rs.getInt("is_active"));
                list.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 5. updateLeaveBalance(int empId, int annual, int sick)
    public static boolean updateLeaveBalance(int empId, int annual, int sick) {
        String sql = "UPDATE leave_balance SET annual_leave=?, sick_leave=? WHERE employee_id=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, annual);
            pstmt.setInt(2, sick);
            pstmt.setInt(3, empId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. updateProfile(Employee emp)
    public static boolean updateProfile(Employee emp) {
        String sql = "UPDATE employees SET first_name = ?, last_name = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, emp.getFirstName());
            pstmt.setString(2, emp.getLastName());
            pstmt.setInt(3, emp.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 7. getFamilyDetails(int employeeId)
    public static List<FamilyDetail> getFamilyDetails(int employeeId) {
        List<FamilyDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM family_details WHERE employee_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new FamilyDetail(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("name"),
                        rs.getString("relationship"), rs.getString("ic_passport")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 8. updateFamilyDetails(int employeeId, List<FamilyDetail> details)
    public static boolean updateFamilyDetails(int employeeId, List<FamilyDetail> details) {
        String del = "DELETE FROM family_details WHERE employee_id = ?";
        String ins = "INSERT INTO family_details (employee_id, name, relationship, ic_passport) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement d = conn.prepareStatement(del)) {
                d.setInt(1, employeeId);
                d.executeUpdate();
            }
            try (PreparedStatement i = conn.prepareStatement(ins)) {
                for (FamilyDetail f : details) {
                    i.setInt(1, employeeId);
                    i.setString(2, f.getName());
                    i.setString(3, f.getRelationship());
                    i.setString(4, f.getIcPassport());
                    i.addBatch();
                }
                i.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 9. checkLeaveBalance(int employeeId, String leaveType)
    public static int checkLeaveBalance(int employeeId, String leaveType) {
        String col = leaveType.equalsIgnoreCase("Annual") ? "annual_leave" : "sick_leave";
        String sql = "SELECT " + col + " FROM leave_balance WHERE employee_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 10. deductLeaveBalance(int employeeId, String leaveType, int days)
    public static boolean deductLeaveBalance(int employeeId, String leaveType, int days) {
        String col = leaveType.equalsIgnoreCase("Annual") ? "annual_leave" : "sick_leave";
        String sql = "UPDATE leave_balance SET " + col + " = " + col + " - ? WHERE employee_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, days);
            pstmt.setInt(2, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 11. applyLeave(LeaveApplication application)
    public static boolean applyLeave(LeaveApplication app) {
        String sql = "INSERT INTO leave_applications (employee_id, leave_type, start_date, end_date, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, app.getEmployeeId());
            pstmt.setString(2, app.getLeaveType());
            pstmt.setString(3, app.getStartDate());
            pstmt.setString(4, app.getEndDate());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 12. getLeaveApplications(int employeeId)
    public static List<LeaveApplication> getLeaveApplications(int employeeId) {
        List<LeaveApplication> list = new ArrayList<>();
        String sql = "SELECT * FROM leave_applications WHERE employee_id = ? ORDER BY applied_date DESC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new LeaveApplication(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("leave_type"),
                        rs.getString("start_date"), rs.getString("end_date"), rs.getString("status"),
                        rs.getString("applied_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 13. getLeaveApplicationsByYear(int employeeId, int year)
    public static List<LeaveApplication> getLeaveApplicationsByYear(int employeeId, int year) {
        List<LeaveApplication> list = new ArrayList<>();
        String sql = "SELECT * FROM leave_applications WHERE employee_id = ? AND YEAR(applied_date) = ? ORDER BY applied_date DESC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new LeaveApplication(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("leave_type"),
                        rs.getString("start_date"), rs.getString("end_date"), rs.getString("status"),
                        rs.getString("applied_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 14. getAllPendingLeaveApplications()
    public static List<LeaveApplication> getAllPendingLeaveApplications() {
        List<LeaveApplication> list = new ArrayList<>();
        String sql = "SELECT * FROM leave_applications WHERE status = 'PENDING' ORDER BY applied_date ASC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new LeaveApplication(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("leave_type"),
                        rs.getString("start_date"), rs.getString("end_date"), rs.getString("status"),
                        rs.getString("applied_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 14.5 getAllLeaveApplications()
    public static List<LeaveApplication> getAllLeaveApplications() {
        List<LeaveApplication> list = new ArrayList<>();
        String sql = "SELECT * FROM leave_applications ORDER BY applied_date DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new LeaveApplication(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("leave_type"),
                        rs.getString("start_date"), rs.getString("end_date"), rs.getString("status"),
                        rs.getString("applied_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 15. updateLeaveStatus(int leaveId, String status)
    public static boolean updateLeaveStatus(int leaveId, String status) {
        String sql = "UPDATE leave_applications SET status = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, leaveId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 16. getEmployeeById(int employeeId)
    public static Employee getEmployeeById(int employeeId) {
        String sql = "SELECT * FROM employees WHERE id = ? AND is_active = 1";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Employee(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("ic_passport"), rs.getString("username"), rs.getString("password"),
                        rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 19. getHRUsername()
    public static String getHRUsername() {
        String sql = "SELECT username FROM employees WHERE role = 'HR' AND is_active = 1 LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "admin.hr";
    }

    // 17. saveChatMessage(String from, String to, String msg)
    public static boolean saveChatMessage(String from, String to, String msg) {
        String sql = "INSERT INTO chat_messages (sender, receiver, message) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, from);
            pstmt.setString(2, to);
            pstmt.setString(3, msg);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 18. getChatHistory(String u1, String u2)
    public static List<ChatMessage> getChatHistory(String u1, String u2) {
        List<ChatMessage> history = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) ORDER BY sent_at ASC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u1);
            pstmt.setString(2, u2);
            pstmt.setString(3, u2);
            pstmt.setString(4, u1);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(new ChatMessage(rs.getInt("id"), rs.getString("sender"), rs.getString("receiver"),
                        rs.getString("message"), rs.getString("sent_at")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
    // 20. getLeaveApplicationById(int leaveId)
    public static LeaveApplication getLeaveApplicationById(int leaveId) {
        String sql = "SELECT * FROM leave_applications WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, leaveId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new LeaveApplication(rs.getInt("id"), rs.getInt("employee_id"), rs.getString("leave_type"),
                        rs.getString("start_date"), rs.getString("end_date"), rs.getString("status"),
                        rs.getString("applied_date"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[DB ERROR] Failed to fetch leave application: " + leaveId, e);
        }
        return null;
    }

    // 21. softDeleteEmployee(int id)
    public static boolean softDeleteEmployee(int employeeId) {
        String sql = "UPDATE employees SET is_active = 0 WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[DB ERROR] Soft delete failed for: " + employeeId, e);
            return false;
        }
    }

    // 22. updateEmployeeProfile(Employee emp)
    public static boolean updateEmployeeProfile(Employee emp) {
        StringBuilder sql = new StringBuilder("UPDATE employees SET first_name = ?, last_name = ?, ic_passport = ?, role = ?, username = ?");
        boolean hasPassword = emp.getPassword() != null && !emp.getPassword().isEmpty();
        if (hasPassword) {
            sql.append(", password = ?");
        }
        sql.append(" WHERE id = ?");
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setString(1, emp.getFirstName());
            pstmt.setString(2, emp.getLastName());
            pstmt.setString(3, emp.getIcPassport());
            pstmt.setString(4, emp.getRole());
            pstmt.setString(5, emp.getUsername());
            
            if (hasPassword) {
                pstmt.setString(6, emp.getPassword());
                pstmt.setInt(7, emp.getId());
            } else {
                pstmt.setInt(6, emp.getId());
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[DB ERROR] Full profile update failed for: " + emp.getId(), e);
            return false;
        }
    }

    // 23. icPassportExists(String ic)
    public static boolean icPassportExists(String ic) {
        String sql = "SELECT COUNT(*) FROM employees WHERE ic_passport = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ic);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 24. changePassword
    public static boolean changePassword(int id, String hashedOld, String hashedNew) {
        String query = "SELECT password FROM employees WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String stored = rs.getString(1);
                if (stored.equals(hashedOld)) {
                    String updateSQL = "UPDATE employees SET password = ? WHERE id = ?";
                    try (PreparedStatement updPstmt = conn.prepareStatement(updateSQL)) {
                        updPstmt.setString(1, hashedNew);
                        updPstmt.setInt(2, id);
                        return updPstmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean recordPayrollUpdate(int empId, int days, String type) {
        String sql = "INSERT INTO payroll_history (employee_id, days_deducted, action_type) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, empId);
            pstmt.setInt(2, days);
            pstmt.setString(3, type);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[DB ERROR] PRS Update recording failed for: " + empId, e);
            return false;
        }
    }

    public static List<String[]> getPayrollHistory(int employeeId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT action_type, days_deducted, process_date FROM payroll_history WHERE employee_id = ? ORDER BY process_date DESC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new String[]{rs.getString(1), String.valueOf(rs.getInt(2)), rs.getString(3)});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}

