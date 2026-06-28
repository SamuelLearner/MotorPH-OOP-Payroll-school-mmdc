# MotorPH OOP Payroll System — Milestone 2

**MO-IT113 Advanced Object-Oriented Programming**

## Team Information
| Field | Details |
|---|---|
| Team Name | Solo Leveling |
| Member | Von Sacher Cabanting — Leader, Programmer |

## Project Overview

A fully functional **OOP-based Java Swing Payroll System** for MotorPH, implementing all four OOP pillars, a clean layered architecture, and **MySQL Database Integration**.

## Features

- **Database Integration (JDBC)** — Fully normalized 19-table MySQL schema for persistent storage of employees, attendance, leave requests, and payroll.
- **Login System** — Credentials stored in MySQL, role-based authentication with SHA-256 password hashing.
- **Role-Based Access Control (RBAC)** — Admin, HR, Finance, IT, Employee roles.
- **Employee Management (CRUD)** — Add, View, Edit, Delete with validation.
- **Payroll Processing** — Monthly (Basic + Allowances) and Hourly (Hours × Rate).
- **Payslip Generation** — GUI view with itemized deductions.
- **Attendance System** — Time In/Out with grace period (8:10 AM).
- **Leave Request System** — Submit, Approve/Reject, History.

## OOP Principles

| Pillar | Implementation |
|---|---|
| **Encapsulation** | Private fields, validated setters in Employee |
| **Abstraction** | 3 interfaces + 2 abstract classes |
| **Inheritance** | Employee → 5 subclasses, GovernmentContribution → 4 subclasses |
| **Polymorphism** | Method overriding (getRole, calculate) + Overloading (computeGrossPay x3, searchEmployee x2) |

## Architecture

```
src/motorph/
├── model/       ← Business objects (interfaces, abstract + concrete classes)
├── dao/         ← Data Access Objects using JDBC (MySQL)
├── service/     ← Business rules and validation
├── ui/          ← JFrame/JPanel interface
├── util/        ← DatabaseConnection, PayrollCalculator
├── test/        ← Console tests
└── Main.java    ← Entry point
data/            ← SQL Schema and Seed Data scripts
```

## How to Compile and Run

1. **Database Setup:**
   Run the following scripts in your local MySQL instance:
   ```bash
   mysql -u root -p < data/schema.sql
   mysql -u root -p motorph_db < data/seed_data.sql
   ```
   *Note: Before compiling the Java application, open `src/motorph/util/DatabaseConnection.java` and change the `PASSWORD` field to match your local MySQL password. It is currently set to an empty placeholder.*

2. **Compile and Run:**
   ```bash
   # Option 1: Use the batch script (Windows)
   run.bat

   # Option 2: Manual (from project root)
   javac -cp "lib/*" -d out -sourcepath src src/motorph/Main.java src/motorph/model/*.java src/motorph/dao/*.java src/motorph/service/*.java src/motorph/ui/*.java src/motorph/util/*.java src/motorph/test/*.java
   java -cp "out;lib/*" motorph.Main
   
   # Run Console Tests
   java -cp "out;lib/*" motorph.test.ConsoleTest
   ```

## Test Credentials

| Employee ID | Password | Role |
|---|---|---|
| 10001 | password123 | Admin |
| 10002 | password123 | HR |
| 10003 | password123 | Finance |
| 10004 | password123 | IT |
| 10005 | password123 | Employee |

## Deduction Formulas

- **SSS** — Bracket-based lookup table
- **PhilHealth** — 5% of salary (employee share = 2.5%), min ₱10K, max ₱100K
- **Pag-IBIG** — 1% if ≤₱1,500; 2% if >₱1,500 (capped at ₱200)
- **Withholding Tax** — BIR progressive brackets on taxable income
