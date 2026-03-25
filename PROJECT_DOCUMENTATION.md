# HRM RMI & Socket System - Project Documentation

## 1. Project Overview
The **Human Resource Management (HRM) System** is a distributed application built using Java. It facilitates essential HR operations such as employee registration, leave management, and real-time internal communication. The system uses a **Hybrid Architecture** combining **RMI (Remote Method Invocation)** for core business logic and **TCP Sockets** for real-time features.

---

## 2. Technical Architecture
The system is divided into three main layers:

### A. Communication Layers
*   **Java RMI (Port 1099)**: Handles all Request-Response operations (Login, Registration, Profile Updates, Reports).
*   **TCP Sockets (Port 12345)**: Handles full-duplex, real-time communication (Chat and Instant Notifications).

### B. Logical Layer
*   **Server**: Centralized logic utilizing synchronized RMI methods to ensure thread safety for concurrent client connections.
*   **DBManager**: A JDBC-based abstraction layer for MySQL operations.

### C. Client Layer (Java Swing)
*   **Premium GUI**: High-contrast Dark Slate theme with emerald and alizarin red accents for a professional look.
*   **Dynamic UI**: Integrated `SwingWorker` for non-blocking network calls and background listeners for real-time socket events.

---

## 3. Database Schema (`hrmdb`)
The system persists data across four normalized tables:

1.  **`employees`**: Stores credentials, names, IC/Passport numbers, and roles (`HR` or `EMPLOYEE`).
2.  **`family_details`**: Stores family members for each employee (One-to-Many).
3.  **`leave_applications`**: Records leave types, dates, and statuses (`PENDING`, `APPROVED`, `REJECTED`).
4.  **`leave_balance`**: Tracks remaining Annual and Sick leave days per employee.

---

### HR Staff Functionality
*   **Employee Registration**: Automatically generates usernames (first.last) and default passwords (`password123`).
*   **Leave Management**: Dedicated tab to approve or reject pending leave requests.
*   **Manual Balance Override**: Ability for HR to manually adjust leave balances for any employee.
*   **Employee List**: A comprehensive view of all registered employees in the system.
*   **Yearly Report Generation**: Generates a text-based summary of profile data, family records, and leave history.
*   **Real-time Chat**: Dedicated panel to message any online employee.
*   **HR Dashboard Statistics**: Live analytics showing Total Employees, Pending Leave Requests, and Approved Leaves in the dashboard header.
*   **Payroll Integration (Simulation)**: Automated communication with a simulated Payroll System (PRS) upon leave approval to process deductions.
*   **Account Deletion (Soft Delete)**: HR can permanently disable employee accounts; inactive accounts are filtered out of lists and blocked from login.
*   **Administrative Profile Update**: HR can directly modify any employee's profile data (Name, IC/Passport, Role) via the management table.

### Employee Functionality
*   **Digital Profile**: Update personal details (First Name, Last Name).
*   **Family Records**: Dynamic table to manage family details with add/remove/save capabilities.
*   **Leave Dashboard**: Visual cards showing current leave balances.
*   **Leave Application**: Integrated form with balance validation.
*   **Real-time Notifications**: Instant popup alerts when HR changes a leave status.
*   **Internal Chat**: Direct messaging window to communicate with HR Admin.
*   **View Profile Refresh**: Ability to fetch the latest profile data (ID, Username, IC/Passport) directly from the server using RMI.

---

## 5. Security Implementation
*   **SHA-256 Hashing**: Passwords are never stored in plain text. They are hashed using the SHA-256 algorithm before being saved to the MySQL database.
*   **Authentication**: During login, the user's input is hashed and compared against the stored hash for secure verification.

---

## 6. Socket Protocol (Real-time)
The custom socket protocol uses a simple text-based format for high performance:
*   `LOGIN:username`: Registers the user's socket connection on the server.
*   `MSG:to:from:message`: Forwards a private message to a specific recipient.
*   `NOTIFY:to:message`: Triggers an automated system notification popup for the recipient.
*   `LOGOUT:username`: Gracefully closes the connection and removes the user from the online list.

---

## 7. How to Run

### Step 1: Compilation
```powershell
javac -d out -cp "lib/mysql-connector-j-9.6.0.jar" src/model/*.java src/remote/*.java src/database/*.java src/server/*.java src/socket/*.java src/client/*.java src/client/ui/*.java
```

### Step 2: Start Server
```powershell
java -cp "out;lib/mysql-connector-j-9.6.0.jar" server.HRMServer
```

### Step 3: Start Client
```powershell
java -cp "out;lib/mysql-connector-j-9.6.0.jar" client.ui.LoginScreen
```

---

## 8. Summary of Completed Improvements
*   **Robustness**: Handled username casing/space issues via normalization.
*   **Stability**: Synchronized all RMI methods to prevent database deadlock.
*   **Fault Tolerance**: Integrated `java.util.logging.Logger` and comprehensive `try-catch` blocks for all database and RMI operations with specific error logging.
*   **Distributed Integration**: Demonstrated inter-system communication via the Payroll System simulation module.
*   **Soft Delete Implementation**: Introduced `is_active` flag for secure account deactivation without data loss.
*   **UX**: Implemented message buffering; messages arrive even if the chat tab isn't open yet.
*   **Analytics**: Integrated live statistics for HR to monitor the system status at a glance.
