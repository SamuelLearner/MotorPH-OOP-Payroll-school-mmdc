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
USE motorph_db;
SET FOREIGN_KEY_CHECKS=0;
-- Roles
INSERT INTO role (role_id, role_name, role_description) VALUES(1, 'Admin', 'Full access'),
(2, 'HR', 'HR Management'),
(3, 'Finance', 'Payroll Management'),
(4, 'IT', 'IT Support'),
(5, 'Employee', 'Basic access');
-- Permissions
INSERT INTO permission (permission_id, access_level, record_type, description) VALUES(1, 'EMPLOYEE_VIEW', 'Employee', ''),
(2, 'EMPLOYEE_ADD', 'Employee', ''),
(3, 'EMPLOYEE_EDIT', 'Employee', ''),
(4, 'EMPLOYEE_DELETE', 'Employee', ''),
(5, 'PAYROLL_PROCESS', 'Payroll', ''),
(6, 'PAYSLIP_VIEW', 'Payroll', ''),
(7, 'ATTENDANCE_VIEW', 'Attendance', ''),
(8, 'ATTENDANCE_MANAGE', 'Attendance', ''),
(9, 'LEAVE_SUBMIT', 'Leave', ''),
(10, 'LEAVE_APPROVE', 'Leave', '');
-- Role Permissions
INSERT INTO role_permission (role_id, permission_id) VALUES(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 9),
(1, 10),
(2, 1),
(2, 2),
(2, 3),
(2, 7),
(2, 8),
(2, 9),
(2, 10),
(2, 6),
(3, 1),
(3, 5),
(3, 6),
(3, 7),
(3, 9),
(4, 1),
(4, 7),
(4, 9),
(4, 6),
(5, 1),
(5, 7),
(5, 9),
(5, 6);
-- Leave Types
INSERT INTO leave_type (leave_type_id, name, is_paid) VALUES(1, 'Sick', 1), (2, 'Vacation', 1), (3, 'Emergency', 0), (4, 'Maternity', 1), (5, 'Paternity', 1);
-- Pay Periods
INSERT INTO pay_period (pay_period_id, start_date, end_date) VALUES(1, '2026-06-01', '2026-06-15'), (2, '2026-06-16', '2026-06-30');
-- Allowances
INSERT INTO allowance (allowance_id, allowance_type, description, benefit_amount) VALUES(1, 'Rice Subsidy', '', 1500.00), (2, 'Phone Allowance', '', 2000.00), (3, 'Clothing Allowance', '', 1000.00);
-- Deductions (SSS sample brackets)
INSERT INTO deduction (deduction_id, deduction_type, lower_amount, upper_amount, tax_base, deduction_rate) VALUES(1, 'SSS', 0, 3250, 0, 135.00),(2, 'SSS', 3250, 3750, 0, 157.50),(3, 'PhilHealth', 10000, 100000, 0, 0.05),(4, 'Pag-IBIG', 0, 1500, 0, 0.01),(5, 'Pag-IBIG', 1500, 999999, 0, 0.02),(6, 'Withholding Tax', 0, 20833, 0, 0.00),(7, 'Withholding Tax', 20833, 33332, 20833, 0.15);
-- Positions
INSERT INTO `position` (position_id, position_name, basic_salary, allowance) VALUES(1, 'Chief Executive Officer', 90000.0, 4500.0),
(2, 'Chief Operating Officer', 60000.0, 4500.0),
(3, 'Chief Finance Officer', 60000.0, 4500.0),
(4, 'Chief Marketing Officer', 60000.0, 4500.0),
(5, 'IT Operations and Systems', 52670.0, 3500.0),
(6, 'HR Manager', 52670.0, 3500.0),
(7, 'HR Team Leader', 42975.0, 3100.0),
(8, 'HR Rank and File', 22500.0, 2500.0),
(9, 'Accounting Head', 52670.0, 3500.0),
(10, 'Payroll Manager', 50825.0, 3500.0),
(11, 'Payroll Team Leader', 38475.0, 3100.0),
(12, 'Payroll Rank and File', 24000.0, 2500.0),
(13, 'Account Manager', 53500.0, 3500.0),
(14, 'Account Team Leader', 42975.0, 3100.0),
(15, 'Account Rank and File', 22500.0, 2500.0),
(16, 'Sales & Marketing', 52670.0, 3500.0),
(17, 'Supply Chain and Logistics', 52670.0, 3500.0),
(18, 'Customer Service and Relations', 52670.0, 3500.0);
-- Addresses
INSERT INTO address (address_id, street, barangay, city, province, zipcode) VALUES(1, 'Valero Carpark Building Valero Street 1227, Makati City', 'N/A', 'N/A', 'N/A', 'N/A'),
(2, 'San Antonio De Padua 2, Block 1 Lot 8 and 2, Dasmarinas, Cavite', 'N/A', 'N/A', 'N/A', 'N/A'),
(3, 'Rm. 402 4/F Jiao Building Timog Avenue Cor. Quezon Avenue 1100, Quezon City', 'N/A', 'N/A', 'N/A', 'N/A'),
(4, '460 Solanda Street Intramuros 1000, Manila', 'N/A', 'N/A', 'N/A', 'N/A'),
(5, 'National Highway, Gingoog,  Misamis Occidental', 'N/A', 'N/A', 'N/A', 'N/A'),
(6, '17/85 Stracke Via Suite 042, Poblacion, Las Piñas 4783 Dinagat Islands', 'N/A', 'N/A', 'N/A', 'N/A'),
(7, '99 Strosin Hills, Poblacion, Bislig 5340 Tawi-Tawi', 'N/A', 'N/A', 'N/A', 'N/A'),
(8, '12A/33 Upton Isle Apt. 420, Roxas City 1814 Surigao del Norte', 'N/A', 'N/A', 'N/A', 'N/A'),
(9, '90A Dibbert Terrace Apt. 190, San Lorenzo 6056 Davao del Norte', 'N/A', 'N/A', 'N/A', 'N/A'),
(10, '#284 T. Morato corner, Scout Rallos Street, Quezon City', 'N/A', 'N/A', 'N/A', 'N/A'),
(11, '93/54 Shanahan Alley Apt. 183, Santo Tomas 1572 Masbate', 'N/A', 'N/A', 'N/A', 'N/A'),
(12, '49 Springs Apt. 266, Poblacion, Taguig 3200 Occidental Mindoro', 'N/A', 'N/A', 'N/A', 'N/A'),
(13, '42/25 Sawayn Stream, Ubay 1208 Zamboanga del Norte', 'N/A', 'N/A', 'N/A', 'N/A'),
(14, '37/46 Kulas Roads, Maragondon 0962 Quirino', 'N/A', 'N/A', 'N/A', 'N/A'),
(15, '22A/52 Lubowitz Meadows, Pililla 4895 Zambales', 'N/A', 'N/A', 'N/A', 'N/A'),
(16, '90 O''Keefe Spur Apt. 379, Catigbian 2772 Sulu', 'N/A', 'N/A', 'N/A', 'N/A'),
(17, '89A Armstrong Trace, Compostela 7874 Maguindanao', 'N/A', 'N/A', 'N/A', 'N/A'),
(18, '08 Grant Drive Suite 406, Poblacion, Iloilo City 9186 La Union', 'N/A', 'N/A', 'N/A', 'N/A'),
(19, '93A/21 Berge Points, Tapaz 2180 Quezon', 'N/A', 'N/A', 'N/A', 'N/A'),
(20, '65 Murphy Center Suite 094, Poblacion, Palayan 5636 Quirino', 'N/A', 'N/A', 'N/A', 'N/A'),
(21, '47A/94 Larkin Plaza Apt. 179, Poblacion, Caloocan 2751 Quirino', 'N/A', 'N/A', 'N/A', 'N/A'),
(22, '06A Gulgowski Extensions, Bongabon 6085 Zamboanga del Sur', 'N/A', 'N/A', 'N/A', 'N/A'),
(23, '99A Padberg Spring, Poblacion, Mabalacat 3959 Lanao del Sur', 'N/A', 'N/A', 'N/A', 'N/A'),
(24, '80A/48 Ledner Ridges, Poblacion, Kabankalan 8870 Marinduque', 'N/A', 'N/A', 'N/A', 'N/A'),
(25, '96/48 Watsica Flats Suite 734, Poblacion, Malolos 1844 Ifugao', 'N/A', 'N/A', 'N/A', 'N/A'),
(26, '58A Wilderman Walks, Poblacion, Digos 5822 Davao del Sur', 'N/A', 'N/A', 'N/A', 'N/A'),
(27, '60 Goyette Valley Suite 219, Poblacion, Tabuk 3159 Lanao del Sur', 'N/A', 'N/A', 'N/A', 'N/A'),
(28, '66/77 Mann Views, Luisiana 1263 Dinagat Islands', 'N/A', 'N/A', 'N/A', 'N/A'),
(29, '72/70 Stamm Spurs, Bustos 4550 Iloilo', 'N/A', 'N/A', 'N/A', 'N/A'),
(30, '50A/83 Bahringer Oval Suite 145, Kiamba 7688 Nueva Ecija', 'N/A', 'N/A', 'N/A', 'N/A'),
(31, '95 Cremin Junction, Surallah 2809 Cotabato', 'N/A', 'N/A', 'N/A', 'N/A'),
(32, 'Hi-way, Yati, Liloan Cebu', 'N/A', 'N/A', 'N/A', 'N/A'),
(33, 'Bulala, Camalaniugan', 'N/A', 'N/A', 'N/A', 'N/A'),
(34, 'Agapita Building, Metro Manila', 'N/A', 'N/A', 'N/A', 'N/A');
-- Employee
INSERT INTO employee (employee_id, last_name, first_name, status, position_id, supervisor_id) VALUES(10001, 'Garcia', 'Manuel III', 'Regular', 1, NULL),
(10002, 'Lim', 'Antonio', 'Regular', 2, 10001),
(10003, 'Aquino', 'Bianca Sofia', 'Regular', 3, 10001),
(10004, 'Reyes', 'Isabella', 'Regular', 4, 10001),
(10005, 'Hernandez', 'Eduard', 'Regular', 5, 10002),
(10006, 'Villanueva', 'Andrea Mae', 'Regular', 6, 10002),
(10007, 'San Jose', 'Brad', 'Regular', 7, 10006),
(10008, 'Romualdez', 'Alice', 'Regular', 8, NULL),
(10009, 'Atienza', 'Rosie', 'Regular', 8, NULL),
(10010, 'Alvaro', 'Roderick', 'Regular', 9, 10003),
(10011, 'Salcedo', 'Anthony', 'Regular', 10, 10010),
(10012, 'Lopez', 'Josie', 'Regular', 11, 10011),
(10013, 'Farala', 'Martha', 'Regular', 12, 10011),
(10014, 'Martinez', 'Leila', 'Regular', 12, 10011),
(10015, 'Romualdez', 'Fredrick', 'Regular', 13, 10002),
(10016, 'Mata', 'Christian', 'Regular', 14, 10015),
(10017, 'De Leon', 'Selena', 'Regular', 14, 10015),
(10018, 'San Jose', 'Allison', 'Regular', 15, 10016),
(10019, 'Rosario', 'Cydney', 'Regular', 15, 10016),
(10020, 'Bautista', 'Mark', 'Regular', 15, 10016),
(10021, 'Lazaro', 'Darlene', 'Probationary', 15, 10016),
(10022, 'Delos Santos', 'Kolby', 'Probationary', 15, 10016),
(10023, 'Santos', 'Vella', 'Probationary', 15, 10016),
(10024, 'Del Rosario', 'Tomas', 'Probationary', 15, 10016),
(10025, 'Tolentino', 'Jacklyn', 'Probationary', 15, 10017),
(10026, 'Gutierrez', 'Percival', 'Probationary', 15, 10017),
(10027, 'Manalaysay', 'Garfield', 'Probationary', 15, 10017),
(10028, 'Villegas', 'Lizeth', 'Probationary', 15, 10017),
(10029, 'Ramos', 'Carol', 'Probationary', 15, 10017),
(10030, 'Maceda', 'Emelia', 'Probationary', 15, 10017),
(10031, 'Aguilar', 'Delia', 'Probationary', 15, 10017),
(10032, 'Castro', 'John Rafael', 'Regular', 16, 10004),
(10033, 'Martinez', 'Carlos Ian', 'Regular', 17, 10004),
(10034, 'Santos', 'Beatriz', 'Regular', 18, 10004);
-- Employee Details
INSERT INTO employee_details (employee_id, dob, ssn, tin, phic, hdmf, phone_number, address_id) VALUES(10001, '1983-10-11', '44-4506057-3', '442-605-657-000', '820126853951', '691295330870', '966-860-270', 1),
(10002, '1988-06-19', '52-2061274-9', '683-102-776-000', '331735646338', '663904995411', '171-867-411', 2),
(10003, '1989-08-04', '30-8870406-2', '971-711-280-000', '177451189665', '171519773969', '966-889-370', 3),
(10004, '1994-06-16', '40-2511815-0', '876-809-437-000', '341911411254', '416946776041', '786-868-477', 4),
(10005, '1989-09-23', '50-5577638-1', '031-702-374-000', '957436191812', '952347222457', '088-861-012', 5),
(10006, '1988-02-14', '49-1632020-8', '317-674-022-000', '382189453145', '441093369646', '918-621-603', 6),
(10007, '1996-03-15', '40-2400714-1', '672-474-690-000', '239192926939', '210850209964', '797-009-261', 7),
(10008, '1992-05-14', '55-4476527-2', '888-572-294-000', '545652640232', '211385556888', '983-606-799', 8),
(10009, '1948-09-24', '41-0644692-3', '604-997-793-000', '708988234853', '260107732354', '266-036-427', 9),
(10010, '1988-03-30', '64-7605054-4', '525-420-419-000', '578114853194', '799254095212', '053-381-386', 10),
(10011, '1993-09-14', '26-9647608-3', '210-805-911-000', '126445315651', '218002473454', '070-766-300', 11),
(10012, '1987-01-14', '44-8563448-3', '218-489-737-000', '431709011012', '113071293354', '478-355-427', 12),
(10013, '1942-01-11', '45-5656375-0', '210-835-851-000', '233693897247', '631130283546', '329-034-366', 13),
(10014, '1970-07-11', '27-2090996-4', '275-792-513-000', '515741057496', '101205445886', '877-110-749', 14),
(10015, '1985-03-10', '26-8768374-1', '598-065-761-000', '308366860059', '223057707853', '023-079-009', 15),
(10016, '1987-10-21', '49-2959312-6', '103-100-522-000', '824187961962', '631052853464', '783-776-744', 16),
(10017, '1975-02-20', '27-2090208-8', '482-259-498-000', '587272469938', '719007608464', '975-432-139', 17),
(10018, '1986-06-24', '45-3251383-0', '121-203-336-000', '745148459521', '114901859343', '179-075-129', 18),
(10019, '1996-10-06', '49-1629900-2', '122-244-511-000', '579253435499', '265104358643', '868-819-912', 19),
(10020, '1991-02-12', '49-1647342-5', '273-970-941-000', '399665157135', '260054585575', '683-725-348', 20),
(10021, '1985-11-25', '45-5617168-2', '354-650-951-000', '606386917510', '104907708845', '740-721-558', 21),
(10022, '1980-02-26', '52-0109570-6', '187-500-345-000', '357451271274', '113017988667', '739-443-033', 22),
(10023, '1983-12-31', '52-9883524-3', '101-558-994-000', '548670482885', '360028104576', '955-879-269', 23),
(10024, '1978-12-18', '45-5866331-6', '560-735-732-000', '953901539995', '913108649964', '882-550-989', 24),
(10025, '1984-05-19', '47-1692793-0', '841-177-857-000', '753800654114', '210546661243', '675-757-366', 25),
(10026, '1970-12-18', '40-9504657-8', '502-995-671-000', '797639382265', '210897095686', '512-899-876', 26),
(10027, '1986-08-28', '45-3298166-4', '336-676-445-000', '810909286264', '211274476563', '948-628-136', 27),
(10028, '1981-12-12', '40-2400719-4', '210-395-397-000', '934389652994', '122238077997', '332-372-215', 28),
(10029, '1978-08-20', '60-1152206-4', '395-032-717-000', '351830469744', '212141893454', '250-700-389', 29),
(10030, '1973-04-14', '54-1331005-0', '215-973-013-000', '465087894112', '515012579765', '973-358-041', 30),
(10031, '1989-01-27', '52-1859253-1', '599-312-588-000', '136451303068', '110018813465', '529-705-439', 31),
(10032, '1992-02-09', '26-7145133-4', '404-768-309-000', '601644902402', '697764069311', '332-424-955', 32),
(10033, '1990-11-16', '11-5062972-7', '256-436-296-000', '380685387212', '993372963726', '078-854-208', 33),
(10034, '1990-08-07', '20-2987501-5', '911-529-713-000', '918460050077', '874042259378', '526-639-511', 34);
-- User Accounts
INSERT INTO user_account (user_id, employee_id, username, password_hash) VALUES(10001, 10001, '10001', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10002, 10002, '10002', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10003, 10003, '10003', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10004, 10004, '10004', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10005, 10005, '10005', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10006, 10006, '10006', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10007, 10007, '10007', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10008, 10008, '10008', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10009, 10009, '10009', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10010, 10010, '10010', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10011, 10011, '10011', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10012, 10012, '10012', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10013, 10013, '10013', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10014, 10014, '10014', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10015, 10015, '10015', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10016, 10016, '10016', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10017, 10017, '10017', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10018, 10018, '10018', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10019, 10019, '10019', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10020, 10020, '10020', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10021, 10021, '10021', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10022, 10022, '10022', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10023, 10023, '10023', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10024, 10024, '10024', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10025, 10025, '10025', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10026, 10026, '10026', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10027, 10027, '10027', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10028, 10028, '10028', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10029, 10029, '10029', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10030, 10030, '10030', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10031, 10031, '10031', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10032, 10032, '10032', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10033, 10033, '10033', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
(10034, 10034, '10034', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');
-- User Roles
INSERT INTO user_role (user_id, role_id) VALUES(10001, 1),
(10002, 1),
(10003, 3),
(10004, 5),
(10005, 4),
(10006, 2),
(10007, 2),
(10008, 2),
(10009, 2),
(10010, 3),
(10011, 3),
(10012, 3),
(10013, 3),
(10014, 3),
(10015, 3),
(10016, 3),
(10017, 3),
(10018, 3),
(10019, 3),
(10020, 3),
(10021, 3),
(10022, 3),
(10023, 3),
(10024, 3),
(10025, 3),
(10026, 3),
(10027, 3),
(10028, 3),
(10029, 3),
(10030, 3),
(10031, 3),
(10032, 5),
(10033, 5),
(10034, 5);
SET FOREIGN_KEY_CHECKS=1;
