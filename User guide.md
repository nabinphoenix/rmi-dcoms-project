# HRM System - User Guide

This guide explains how to properly set up, run, and manage the Human Resource Management System.

---

## 1. Prerequisites
To run the system, you must have the following installed:
*   **Java JDK (v8 or higher)**
*   **XAMPP Control Panel**
*   **MySQL Connector JAR** (included in `lib/` directory)

---

## 2. How to Run the System (Step-by-Step)

Follow these steps in the exact order:

### Step 1: Start XAMPP Services
1.  Open the **XAMPP Control Panel**.
2.  Start **MySQL** (Required for database access).
3.  Start **Apache** (Required to view tables in your browser).

### Step 2: Start the Server
Open a terminal in the project directory and run the following command to start the RMI and Chat server:
```powershell
java -cp "out;lib/mysql-connector-j-9.6.0.jar" server.HRMServer
```
*Wait for the message: "Server is ready and waiting for client connections..."*

### Step 3: Start the Client (Login Screen)
Open a new terminal and run the following command to open the application:
```powershell
java -cp "out;lib/mysql-connector-j-9.6.0.jar" client.ui.LoginScreen
```

---

## 3. How to View and Manage Table Data

### Method A: Using a Web Browser (Recommended)
This is the easiest way to see your data in a clean, visual table.
1.  Ensure **Apache** and **MySQL** are both running in XAMPP.
2.  Open your browser and navigate to: `http://localhost/phpmyadmin/`
3.  On the left sidebar, click on your database (e.g., `hrm_db`).
4.  Click on any table (like `employees` or `leave_applications`) to view the records.

### Method B: Using the XAMPP Shell (Command Line)
If Apache is not running, you can still use the text-based shell:
1.  Click the **Shell** button on the right side of the XAMPP Control Panel.
2.  In the black terminal, type the following commands:
    *   Login: `mysql -u root`
    *   Select Database: `use hrm_db;`
    *   View Employees: `select * from employees;`
    *   View Leave Status: `select * from leave_applications;`

---

## 4. Troubleshooting
*   **"Connection Refused":** Make sure the `HRMServer` is running before you try to log in.
*   **"Login Fails":** Check that **MySQL** is started in XAMPP.
*   **"Apache Port Error":** If Apache won't start, ensure no other web server (like IIS) is using port 80.
