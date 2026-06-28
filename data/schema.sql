-- MotorPH Payroll System Database Schema
-- Matches the provided Schema Diagram for Presentation

DROP DATABASE IF EXISTS motorph_db;
CREATE DATABASE motorph_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE motorph_db;

-- Lookup / Reference Tables

-- Position table
CREATE TABLE `position` (
    position_id INT AUTO_INCREMENT PRIMARY KEY,
    position_name VARCHAR(50) UNIQUE NOT NULL,
    basic_salary DECIMAL(12,2) NOT NULL,
    allowance DECIMAL(12,2) NOT NULL
) ENGINE=InnoDB;

-- Address table
CREATE TABLE address (
    address_id INT AUTO_INCREMENT PRIMARY KEY,
    street VARCHAR(100) NOT NULL,
    barangay VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL,
    zipcode VARCHAR(10) NOT NULL
) ENGINE=InnoDB;

-- Leave_Type table
CREATE TABLE leave_type (
    leave_type_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    is_paid BOOLEAN NOT NULL
) ENGINE=InnoDB;

-- Pay_Period table
CREATE TABLE pay_period (
    pay_period_id INT AUTO_INCREMENT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL
) ENGINE=InnoDB;

-- Deduction reference table (SSS/PhilHealth/PagIBIG/Tax brackets)
CREATE TABLE deduction (
    deduction_id INT AUTO_INCREMENT PRIMARY KEY,
    deduction_type VARCHAR(50) NOT NULL,
    lower_amount DECIMAL(12,2) NOT NULL,
    upper_amount DECIMAL(12,2) NULL,
    tax_base DECIMAL(12,2) NOT NULL,
    deduction_rate DECIMAL(5,2) NOT NULL
) ENGINE=InnoDB;

-- Allowance reference table
CREATE TABLE allowance (
    allowance_id INT AUTO_INCREMENT PRIMARY KEY,
    allowance_type VARCHAR(50) NOT NULL,
    description VARCHAR(150),
    benefit_amount DECIMAL(12,2) NOT NULL
) ENGINE=InnoDB;

-- Role table
CREATE TABLE role (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    role_description VARCHAR(150),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Permission table
CREATE TABLE permission (
    permission_id INT AUTO_INCREMENT PRIMARY KEY,
    access_level VARCHAR(20) NOT NULL,
    record_type VARCHAR(50) NOT NULL,
    description VARCHAR(150)
) ENGINE=InnoDB;

-- Role_Permission junction table
CREATE TABLE role_permission (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(role_id),
    FOREIGN KEY (permission_id) REFERENCES permission(permission_id)
) ENGINE=InnoDB;

-- Core Entity Tables

-- Employee table
CREATE TABLE employee (
    employee_id INT PRIMARY KEY,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    position_id INT,
    supervisor_id INT,
    FOREIGN KEY (position_id) REFERENCES `position`(position_id),
    FOREIGN KEY (supervisor_id) REFERENCES employee(employee_id)
) ENGINE=InnoDB;

-- Employee_Details table (1:1 with Employee)
CREATE TABLE employee_details (
    employee_id INT PRIMARY KEY,
    dob DATE NOT NULL,
    ssn VARCHAR(20) UNIQUE NOT NULL,
    tin VARCHAR(20) UNIQUE NOT NULL,
    phic VARCHAR(20) UNIQUE NOT NULL,
    hdmf VARCHAR(20) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    address_id INT,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    FOREIGN KEY (address_id) REFERENCES address(address_id)
) ENGINE=InnoDB;

-- User_Account table
CREATE TABLE user_account (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    password_hash VARCHAR(255) NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
) ENGINE=InnoDB;

-- User_Role junction table
CREATE TABLE user_role (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user_account(user_id),
    FOREIGN KEY (role_id) REFERENCES role(role_id)
) ENGINE=InnoDB;

-- Operational Tables

-- Attendance table
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    time_in DATETIME NOT NULL,
    time_out DATETIME NULL,
    pay_period_id INT,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    FOREIGN KEY (pay_period_id) REFERENCES pay_period(pay_period_id)
) ENGINE=InnoDB;

-- Overtime table
CREATE TABLE overtime (
    overtime_id INT AUTO_INCREMENT PRIMARY KEY,
    attendance_id INT NOT NULL,
    approved_by INT,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_time DATETIME NULL,
    FOREIGN KEY (attendance_id) REFERENCES attendance(attendance_id),
    FOREIGN KEY (approved_by) REFERENCES employee(employee_id)
) ENGINE=InnoDB;

-- Leave_Request table
CREATE TABLE leave_request (
    leave_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(500),
    leave_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by INT,
    pay_period_id INT,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    FOREIGN KEY (leave_type_id) REFERENCES leave_type(leave_type_id),
    FOREIGN KEY (approved_by) REFERENCES employee(employee_id),
    FOREIGN KEY (pay_period_id) REFERENCES pay_period(pay_period_id)
) ENGINE=InnoDB;

-- Payroll Tables

-- Payroll table
CREATE TABLE payroll (
    payroll_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    pay_period_id INT NOT NULL,
    gross_pay DECIMAL(12,2) NOT NULL,
    basic_pay DECIMAL(12,2) NOT NULL,
    total_allowance DECIMAL(12,2) NOT NULL,
    total_deduction DECIMAL(12,2) NOT NULL,
    net_pay DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employee(employee_id),
    FOREIGN KEY (pay_period_id) REFERENCES pay_period(pay_period_id)
) ENGINE=InnoDB;

-- Payroll_Deduction junction table
CREATE TABLE payroll_deduction (
    payroll_id INT NOT NULL,
    deduction_id INT NOT NULL,
    deduction_amount DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (payroll_id, deduction_id),
    FOREIGN KEY (payroll_id) REFERENCES payroll(payroll_id),
    FOREIGN KEY (deduction_id) REFERENCES deduction(deduction_id)
) ENGINE=InnoDB;

-- Payroll_Allowance junction table
CREATE TABLE payroll_allowance (
    payroll_id INT NOT NULL,
    allowance_id INT NOT NULL,
    benefit_amount DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (payroll_id, allowance_id),
    FOREIGN KEY (payroll_id) REFERENCES payroll(payroll_id),
    FOREIGN KEY (allowance_id) REFERENCES allowance(allowance_id)
) ENGINE=InnoDB;
