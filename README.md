# HRM-RMI-System

## Human Resource Management System using Java RMI

A complete distributed HRM system built with Java RMI, Swing GUI, and MySQL database.

---

## Tech Stack

| Component      | Technology                   |
|----------------|------------------------------|
| Language       | Java (JDK 17+)               |
| GUI            | Java Swing                   |
| Database       | MySQL                        |
| JDBC Driver    | mysql-connector-j-9.6.0.jar  |
| RMI            | Java built-in RMI            |
| Security       | SHA-256 password hashing      |

---

## Project Structure

```
HRM-RMI-System/
├── src/
│   ├── remote/
│   │   └── HRMService.java          # RMI Remote Interface
│   ├── server/
│   │   ├── HRMServiceImpl.java       # RMI Service Implementation
│   │   └── HRMServer.java            # RMI Server Entry Point
│   ├── client/
│   │   ├── HRMClient.java            # RMI Client Connector
│   │   └── ui/
│   │       ├── LoginScreen.java      # Login GUI
│   │       ├── HRDashboard.java      # HR Staff Dashboard
│   │       └── EmployeeDashboard.java # Employee Dashboard
│   ├── model/
│   │   ├── Employee.java             # Employee Model
│   │   ├── LeaveApplication.java     # Leave Application Model
│   │   └── FamilyDetail.java         # Family Detail Model
│   └── database/
│       └── DBManager.java            # Database Operations
├── lib/
│   └── mysql-connector-j-9.6.0.jar   # MySQL JDBC Driver
└── README.md
```

---

## Prerequisites

1. **JDK 17 or above** installed and `JAVA_HOME` configured
2. **MySQL Server** installed and running
3. **mysql-connector-j-9.6.0.jar** placed in the `lib/` directory

Download the MySQL Connector/J from:
https://dev.mysql.com/downloads/connector/j/

---

## Database Setup

### Step 1: Open MySQL command line or MySQL Workbench

### Step 2: Create the database and tables

```sql
CREATE DATABASE IF NOT EXISTS hrmdb;
USE hrmdb;

CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    ic_passport VARCHAR(20) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('HR', 'EMPLOYEE') NOT NULL,
    is_active TINYINT(1) DEFAULT 1
);

CREATE TABLE family_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    name VARCHAR(100),
    relationship VARCHAR(50),
    ic_passport VARCHAR(20),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE leave_applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT,
    leave_type VARCHAR(50),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    applied_date DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE leave_balance (
    employee_id INT PRIMARY KEY,
    annual_leave INT DEFAULT 14,
    sick_leave INT DEFAULT 14,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);
```

### Step 3: Insert a default HR user

The password below is the SHA-256 hash of `admin123`:

```sql
INSERT INTO employees (first_name, last_name, ic_passport, username, password, role, is_active)
VALUES ('Admin', 'HR', 'HR-ADMIN-001', 'admin.hr',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'HR', 1);
```

### Step 4: Configure database password

Open `src/database/DBManager.java` and set your MySQL root password:

```java
private static final String PASSWORD = "";  // <-- Set your MySQL root password here
```

---

## Compilation & Run Instructions (Windows - VS Code Terminal)

### Step 1: Open terminal in the `HRM-RMI-System` directory

### Step 2: Compile all Java files

```powershell
javac -d out -cp "lib/mysql-connector-j-9.6.0.jar" src/model/*.java src/remote/*.java src/database/*.java src/server/*.java src/client/*.java src/client/ui/*.java
```

### Step 3: Start the RMI Server

```powershell
java -cp "out;lib/mysql-connector-j-9.6.0.jar" server.HRMServer
```

You should see:
```
==============================================
   HRM RMI Server started successfully!
   Registry running on port 1099
   Service bound as: HRMService
==============================================
Server is ready and waiting for client connections...
```

### Step 4: Start the Client (in a NEW terminal window)

```powershell
java -cp "out;lib/mysql-connector-j-9.6.0.jar" client.ui.LoginScreen
```

---

## Default Login Credentials

| Role     | Username  | Password   |
|----------|-----------|------------|
| HR Staff | admin.hr  | admin123   |

> New employees registered by HR will have:
> - Username: `firstname.lastname` (auto-generated)
> - Default Password: `password123`

---

## Features

### HR Staff Features
1. **Register New Employee** — Enter first name, last name, and IC/Passport number. The system auto-generates a username and default password, and creates a leave balance record (14 annual + 14 sick days).
2. **Generate Yearly Report** — Enter an Employee ID and year to view a comprehensive report including profile, family details, and leave history.

### Employee Features
1. **Login** — Authenticate with username and password
2. **Update Profile** — Edit first name and last name
3. **Family Details** — Add, edit, and save family member information
4. **Leave Balance** — View remaining annual and sick leave days
5. **Apply for Leave** — Submit leave applications with date selection
6. **Leave Status** — View all leave applications and their statuses

---

## Security

- All passwords are hashed using **SHA-256** before storage
- Login validates the hashed password against the stored hash
- Plain text passwords are **never** stored or displayed

---

## Architecture Overview

```
┌─────────────┐         RMI (Port 1099)         ┌─────────────┐
│   Client     │ ◄──────────────────────────────► │   Server     │
│  (Swing UI)  │    Remote Method Invocation      │ (RMI Service)│
│              │                                   │              │
│ LoginScreen  │    HRMService (Remote Interface)  │ HRMServiceImpl│
│ HRDashboard  │                                   │              │
│ EmployeeDash │                                   │  DBManager   │
└─────────────┘                                   └──────┬───────┘
                                                         │ JDBC
                                                   ┌─────▼──────┐
                                                   │   MySQL DB   │
                                                   │   (hrmdb)    │
                                                   └─────────────┘
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Ensure `mysql-connector-j-9.6.0.jar` is in the `lib/` folder and included in the classpath |
| `java.rmi.ConnectException` | Make sure the RMI server is running before starting the client |
| `SQLException: Access denied` | Check the MySQL username/password in `DBManager.java` |
| `Port 1099 already in use` | Another RMI registry or process is using port 1099. Kill it or change the port |
