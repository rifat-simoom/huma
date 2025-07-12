package com.hrm.service;

import com.hrm.entity.Employee;
import com.hrm.entity.enums.EmployeeStatus;
import com.hrm.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public Optional<Employee> getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId);
    }

    public Employee saveEmployee(Employee employee) {
        validateEmployee(employee);
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        employee.setFirstName(employeeDetails.getFirstName());
        employee.setLastName(employeeDetails.getLastName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setPhoneNumber(employeeDetails.getPhoneNumber());
        employee.setPosition(employeeDetails.getPosition());
        employee.setSalary(employeeDetails.getSalary());
        employee.setDepartment(employeeDetails.getDepartment());
        employee.setManager(employeeDetails.getManager());
        employee.setStatus(employeeDetails.getStatus());
        employee.setAnnualLeaveBalance(employeeDetails.getAnnualLeaveBalance());
        employee.setSickLeaveBalance(employeeDetails.getSickLeaveBalance());

        validateEmployee(employee);
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        // Soft delete by setting status to TERMINATED
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);
    }

    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }

    public List<Employee> getEmployeesByManager(Long managerId) {
        return employeeRepository.findByManagerId(managerId);
    }

    public List<Employee> getEmployeesByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status);
    }

    public List<Employee> getActiveEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentIdAndStatus(departmentId, EmployeeStatus.ACTIVE);
    }

    public Page<Employee> searchEmployees(String searchTerm, Pageable pageable) {
        return employeeRepository.searchEmployees(searchTerm, pageable);
    }

    public Long countActiveEmployeesByDepartment(Long departmentId) {
        return employeeRepository.countActiveEmployeesByDepartment(departmentId);
    }

    public List<Employee> getActiveDirectReports(Long managerId) {
        return employeeRepository.findActiveDirectReports(managerId);
    }

    public boolean isEmailAlreadyExists(String email) {
        return employeeRepository.findByEmail(email).isPresent();
    }

    public boolean isEmployeeIdAlreadyExists(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId).isPresent();
    }

    public void activateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employee.setStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(employee);
    }

    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
    }

    public void updateLeaveBalance(Long employeeId, double annualLeaveBalance, double sickLeaveBalance) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
        
        employee.setAnnualLeaveBalance(annualLeaveBalance);
        employee.setSickLeaveBalance(sickLeaveBalance);
        employeeRepository.save(employee);
    }

    private void validateEmployee(Employee employee) {
        if (employee.getEmail() != null) {
            Optional<Employee> existingEmployee = employeeRepository.findByEmail(employee.getEmail());
            if (existingEmployee.isPresent() && !existingEmployee.get().getId().equals(employee.getId())) {
                throw new RuntimeException("Email already exists: " + employee.getEmail());
            }
        }

        if (employee.getEmployeeId() != null) {
            Optional<Employee> existingEmployee = employeeRepository.findByEmployeeId(employee.getEmployeeId());
            if (existingEmployee.isPresent() && !existingEmployee.get().getId().equals(employee.getId())) {
                throw new RuntimeException("Employee ID already exists: " + employee.getEmployeeId());
            }
        }

        // Validate that manager is not the employee themselves
        if (employee.getManager() != null && employee.getId() != null && 
            employee.getManager().getId().equals(employee.getId())) {
            throw new RuntimeException("Employee cannot be their own manager");
        }
    }
}