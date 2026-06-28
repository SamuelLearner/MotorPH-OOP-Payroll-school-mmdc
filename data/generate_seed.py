import csv
import hashlib
from datetime import datetime
import re

CSV_FILE = r"C:\Users\Vonstitution\Downloads\Copy of MotorPH_Employee Data - Employee Details.csv"
OUT_FILE = r"C:\Users\Vonstitution\Downloads\MotorPH-OOP-Payroll\data\seed_data.sql"

def parse_money(s):
    if not s: return 0.0
    return float(str(s).replace(',', '').replace('"', '').strip())

def parse_date(s):
    try:
        return datetime.strptime(s.strip(), '%m/%d/%Y').strftime('%Y-%m-%d')
    except:
        return '1970-01-01'

def sha256(pwd):
    return hashlib.sha256(pwd.encode('utf-8')).hexdigest()

def get_role_id(position):
    pos = position.lower()
    if 'chief' in pos and 'finance' not in pos and 'marketing' not in pos:
        return 1 # Admin
    elif 'hr' in pos:
        return 2 # HR
    elif 'finance' in pos or 'account' in pos or 'payroll' in pos:
        return 3 # Finance
    elif 'it' in pos:
        return 4 # IT
    else:
        return 5 # Employee

with open(CSV_FILE, mode='r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    rows = list(reader)

# For mapping supervisors
emp_name_to_id = {}
for r in rows:
    last = r['Last Name'].strip()
    first = r['First Name'].strip()
    # Handle supervisor format from CSV "Last, First"
    emp_name_to_id[f"{last}, {first}"] = int(r['Employee #'])
    
# We might need to handle slight mismatches
def get_supervisor_id(name):
    if not name or name == 'N/A' or name == 'n/a': return 'NULL'
    name = name.replace('"', '').strip()
    return emp_name_to_id.get(name, 'NULL')

sql = []
sql.append("USE motorph_db;\n")
sql.append("SET FOREIGN_KEY_CHECKS=0;\n")

# 1. Reference Data
sql.append("-- Roles")
roles = [
    (1, 'Admin', 'Full access'),
    (2, 'HR', 'HR Management'),
    (3, 'Finance', 'Payroll Management'),
    (4, 'IT', 'IT Support'),
    (5, 'Employee', 'Basic access')
]
sql.append("INSERT INTO role (role_id, role_name, role_description) VALUES")
sql.append(",\n".join([f"({id}, '{name}', '{desc}')" for id, name, desc in roles]) + ";\n")

sql.append("-- Permissions")
permissions = [
    (1, 'EMPLOYEE_VIEW', 'Employee', ''),
    (2, 'EMPLOYEE_ADD', 'Employee', ''),
    (3, 'EMPLOYEE_EDIT', 'Employee', ''),
    (4, 'EMPLOYEE_DELETE', 'Employee', ''),
    (5, 'PAYROLL_PROCESS', 'Payroll', ''),
    (6, 'PAYSLIP_VIEW', 'Payroll', ''),
    (7, 'ATTENDANCE_VIEW', 'Attendance', ''),
    (8, 'ATTENDANCE_MANAGE', 'Attendance', ''),
    (9, 'LEAVE_SUBMIT', 'Leave', ''),
    (10, 'LEAVE_APPROVE', 'Leave', '')
]
sql.append("INSERT INTO permission (permission_id, access_level, record_type, description) VALUES")
sql.append(",\n".join([f"({id}, '{lvl}', '{rec}', '{desc}')" for id, lvl, rec, desc in permissions]) + ";\n")

sql.append("-- Role Permissions")
# Admin: all
rp = []
for p in range(1, 11): rp.append((1, p))
# HR: 1,2,3,7,8,9,10,6
for p in [1,2,3,7,8,9,10,6]: rp.append((2, p))
# Finance: 1,5,6,7,9
for p in [1,5,6,7,9]: rp.append((3, p))
# IT: 1,7,9,6
for p in [1,7,9,6]: rp.append((4, p))
# Employee: 1,7,9,6
for p in [1,7,9,6]: rp.append((5, p))

sql.append("INSERT INTO role_permission (role_id, permission_id) VALUES")
sql.append(",\n".join([f"({r}, {p})" for r, p in rp]) + ";\n")

sql.append("-- Leave Types")
sql.append("INSERT INTO leave_type (leave_type_id, name, is_paid) VALUES")
sql.append("(1, 'Sick', 1), (2, 'Vacation', 1), (3, 'Emergency', 0), (4, 'Maternity', 1), (5, 'Paternity', 1);\n")

sql.append("-- Pay Periods")
sql.append("INSERT INTO pay_period (pay_period_id, start_date, end_date) VALUES")
sql.append("(1, '2026-06-01', '2026-06-15'), (2, '2026-06-16', '2026-06-30');\n")

sql.append("-- Allowances")
sql.append("INSERT INTO allowance (allowance_id, allowance_type, description, benefit_amount) VALUES")
sql.append("(1, 'Rice Subsidy', '', 1500.00), (2, 'Phone Allowance', '', 2000.00), (3, 'Clothing Allowance', '', 1000.00);\n")

sql.append("-- Deductions (SSS sample brackets)")
sql.append("INSERT INTO deduction (deduction_id, deduction_type, lower_amount, upper_amount, tax_base, deduction_rate) VALUES")
sql.append("(1, 'SSS', 0, 3250, 0, 135.00),")
sql.append("(2, 'SSS', 3250, 3750, 0, 157.50),")
sql.append("(3, 'PhilHealth', 10000, 100000, 0, 0.05),")
sql.append("(4, 'Pag-IBIG', 0, 1500, 0, 0.01),")
sql.append("(5, 'Pag-IBIG', 1500, 999999, 0, 0.02),")
sql.append("(6, 'Withholding Tax', 0, 20833, 0, 0.00),")
sql.append("(7, 'Withholding Tax', 20833, 33332, 20833, 0.15);\n")

# Process Employees
positions = {}
addresses = []
employee_data = []
emp_details = []
accounts = []
user_roles = []

addr_id = 1
pos_id = 1
for row in rows:
    emp_id = int(row['Employee #'])
    pos_name = row['Position'].strip()
    basic = parse_money(row['Basic Salary'])
    allow = parse_money(row['Rice Subsidy']) + parse_money(row['Phone Allowance']) + parse_money(row['Clothing Allowance'])
    
    if pos_name not in positions:
        positions[pos_name] = {'id': pos_id, 'basic': basic, 'allow': allow}
        pos_id += 1
        
    p_id = positions[pos_name]['id']
    
    # Address
    # We will just put the whole string in street, and N/A for others for simplicity
    street = row['Address'].replace("'", "''").strip()
    addresses.append(f"({addr_id}, '{street}', 'N/A', 'N/A', 'N/A', 'N/A')")
    
    # Employee
    last = row['Last Name'].replace("'", "''").strip()
    first = row['First Name'].replace("'", "''").strip()
    status = row['Status'].strip()
    sup_id = get_supervisor_id(row['Immediate Supervisor'])
    
    employee_data.append(f"({emp_id}, '{last}', '{first}', '{status}', {p_id}, {sup_id})")
    
    # Employee details
    dob = parse_date(row['Birthday'])
    ssn = row['SSS #'].strip()
    tin = row['TIN #'].strip()
    phic = row['Philhealth #'].strip()
    hdmf = row['Pag-ibig #'].strip()
    phone = row['Phone Number'].strip()
    emp_details.append(f"({emp_id}, '{dob}', '{ssn}', '{tin}', '{phic}', '{hdmf}', '{phone}', {addr_id})")
    
    # Account
    pwd_hash = sha256('password123')
    username = str(emp_id)
    accounts.append(f"({emp_id}, {emp_id}, '{username}', '{pwd_hash}')")
    
    # User Role
    r_id = get_role_id(pos_name)
    user_roles.append(f"({emp_id}, {r_id})")
    
    addr_id += 1

sql.append("-- Positions")
sql.append("INSERT INTO position (position_id, position_name, basic_salary, allowance) VALUES")
pos_vals = [f"({v['id']}, '{k}', {v['basic']}, {v['allow']})" for k, v in positions.items()]
sql.append(",\n".join(pos_vals) + ";\n")

sql.append("-- Addresses")
sql.append("INSERT INTO address (address_id, street, barangay, city, province, zipcode) VALUES")
sql.append(",\n".join(addresses) + ";\n")

sql.append("-- Employee")
sql.append("INSERT INTO employee (employee_id, last_name, first_name, status, position_id, supervisor_id) VALUES")
sql.append(",\n".join(employee_data) + ";\n")

sql.append("-- Employee Details")
sql.append("INSERT INTO employee_details (employee_id, dob, ssn, tin, phic, hdmf, phone_number, address_id) VALUES")
sql.append(",\n".join(emp_details) + ";\n")

sql.append("-- User Accounts")
sql.append("INSERT INTO user_account (user_id, employee_id, username, password_hash) VALUES")
sql.append(",\n".join(accounts) + ";\n")

sql.append("-- User Roles")
sql.append("INSERT INTO user_role (user_id, role_id) VALUES")
sql.append(",\n".join(user_roles) + ";\n")

sql.append("SET FOREIGN_KEY_CHECKS=1;\n")

with open(OUT_FILE, 'w', encoding='utf-8') as f:
    f.writelines(sql)

print(f"Generated {OUT_FILE} successfully!")
