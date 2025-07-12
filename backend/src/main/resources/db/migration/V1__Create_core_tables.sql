-- Create departments table
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    code VARCHAR(20) NOT NULL UNIQUE,
    head_id BIGINT,
    parent_department_id BIGINT,
    location VARCHAR(255),
    budget BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

-- Create employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(20),
    hire_date DATE NOT NULL,
    termination_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    job_title VARCHAR(100) NOT NULL,
    department_id BIGINT,
    manager_id BIGINT,
    salary DECIMAL(10,2),
    address VARCHAR(255),
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(50),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relationship VARCHAR(50),
    profile_picture_url VARCHAR(255),
    keycloak_user_id VARCHAR(255),
    annual_leave_balance INTEGER DEFAULT 20,
    sick_leave_balance INTEGER DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_employee_manager FOREIGN KEY (manager_id) REFERENCES employees(id)
);

-- Create leave_requests table
CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    days_requested INTEGER NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approver_id BIGINT,
    approver_comments VARCHAR(1000),
    approved_date DATE,
    airflow_dag_run_id VARCHAR(255),
    airflow_task_id VARCHAR(255),
    is_half_day BOOLEAN DEFAULT FALSE,
    emergency_contact_notified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_leave_request_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_leave_request_approver FOREIGN KEY (approver_id) REFERENCES employees(id)
);

-- Create leave_request_attachments table
CREATE TABLE leave_request_attachments (
    leave_request_id BIGINT NOT NULL,
    attachment_url VARCHAR(255),
    CONSTRAINT fk_leave_attachment_request FOREIGN KEY (leave_request_id) REFERENCES leave_requests(id)
);

-- Create attendance table
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    break_start_time TIMESTAMP,
    break_end_time TIMESTAMP,
    total_hours_worked DOUBLE PRECISION,
    break_duration_minutes INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'ABSENT',
    is_overtime BOOLEAN DEFAULT FALSE,
    overtime_hours DOUBLE PRECISION,
    notes VARCHAR(500),
    location VARCHAR(255),
    ip_address VARCHAR(45),
    is_remote BOOLEAN DEFAULT FALSE,
    approved_by_manager BOOLEAN DEFAULT FALSE,
    approved_by_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_attendance_approver FOREIGN KEY (approved_by_id) REFERENCES employees(id),
    CONSTRAINT uk_attendance_employee_date UNIQUE (employee_id, date)
);

-- Add foreign key constraints for departments (after employees table is created)
ALTER TABLE departments ADD CONSTRAINT fk_department_head FOREIGN KEY (head_id) REFERENCES employees(id);
ALTER TABLE departments ADD CONSTRAINT fk_department_parent FOREIGN KEY (parent_department_id) REFERENCES departments(id);

-- Create indexes for better performance
CREATE INDEX idx_employees_department ON employees(department_id);
CREATE INDEX idx_employees_manager ON employees(manager_id);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_employees_employee_id ON employees(employee_id);

CREATE INDEX idx_leave_requests_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_leave_requests_start_date ON leave_requests(start_date);
CREATE INDEX idx_leave_requests_approver ON leave_requests(approver_id);

CREATE INDEX idx_attendance_employee ON attendance(employee_id);
CREATE INDEX idx_attendance_date ON attendance(date);
CREATE INDEX idx_attendance_employee_date ON attendance(employee_id, date);

CREATE INDEX idx_departments_code ON departments(code);
CREATE INDEX idx_departments_parent ON departments(parent_department_id);
CREATE INDEX idx_departments_head ON departments(head_id);