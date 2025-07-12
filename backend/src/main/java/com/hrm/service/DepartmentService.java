package com.hrm.service;

import com.hrm.entity.Department;
import com.hrm.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    public Optional<Department> getDepartmentByName(String name) {
        return departmentRepository.findByName(name);
    }

    public Department saveDepartment(Department department) {
        validateDepartment(department);
        return departmentRepository.save(department);
    }

    public Department updateDepartment(Long id, Department departmentDetails) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        department.setName(departmentDetails.getName());
        department.setDescription(departmentDetails.getDescription());
        department.setParentDepartment(departmentDetails.getParentDepartment());
        department.setManagerId(departmentDetails.getManagerId());
        department.setBudget(departmentDetails.getBudget());
        department.setLocation(departmentDetails.getLocation());

        validateDepartment(department);
        return departmentRepository.save(department);
    }

    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        // Check if department has employees
        Long employeeCount = departmentRepository.countEmployeesInDepartment(id);
        if (employeeCount > 0) {
            throw new RuntimeException("Cannot delete department with existing employees. " +
                    "Please reassign " + employeeCount + " employees first.");
        }

        // Check if department has child departments
        List<Department> childDepartments = departmentRepository.findByParentDepartmentId(id);
        if (!childDepartments.isEmpty()) {
            throw new RuntimeException("Cannot delete department with child departments. " +
                    "Please reassign " + childDepartments.size() + " child departments first.");
        }

        departmentRepository.delete(department);
    }

    public List<Department> getChildDepartments(Long parentDepartmentId) {
        return departmentRepository.findByParentDepartmentId(parentDepartmentId);
    }

    public List<Department> getRootDepartments() {
        return departmentRepository.findRootDepartments();
    }

    public List<Department> getDepartmentsByManager(Long managerId) {
        return departmentRepository.findByManagerId(managerId);
    }

    public List<Department> searchDepartments(String searchTerm) {
        return departmentRepository.searchDepartments(searchTerm);
    }

    public Long countEmployeesInDepartment(Long departmentId) {
        return departmentRepository.countEmployeesInDepartment(departmentId);
    }

    public boolean isDepartmentNameExists(String name) {
        return departmentRepository.findByName(name).isPresent();
    }

    public List<Department> getDepartmentHierarchy(Long departmentId) {
        // Get the department and all its descendants
        List<Department> hierarchy = List.of();
        Department department = departmentRepository.findById(departmentId).orElse(null);
        
        if (department != null) {
            hierarchy.add(department);
            addChildDepartmentsToHierarchy(department.getId(), hierarchy);
        }
        
        return hierarchy;
    }

    private void addChildDepartmentsToHierarchy(Long parentId, List<Department> hierarchy) {
        List<Department> children = departmentRepository.findByParentDepartmentId(parentId);
        for (Department child : children) {
            hierarchy.add(child);
            addChildDepartmentsToHierarchy(child.getId(), hierarchy);
        }
    }

    public boolean isValidParentDepartment(Long departmentId, Long parentDepartmentId) {
        if (departmentId.equals(parentDepartmentId)) {
            return false; // Department cannot be its own parent
        }

        // Check if setting this parent would create a circular reference
        Department potentialParent = departmentRepository.findById(parentDepartmentId).orElse(null);
        while (potentialParent != null) {
            if (potentialParent.getId().equals(departmentId)) {
                return false; // Circular reference detected
            }
            potentialParent = potentialParent.getParentDepartment();
        }

        return true;
    }

    private void validateDepartment(Department department) {
        if (department.getName() != null) {
            Optional<Department> existingDepartment = departmentRepository.findByName(department.getName());
            if (existingDepartment.isPresent() && !existingDepartment.get().getId().equals(department.getId())) {
                throw new RuntimeException("Department name already exists: " + department.getName());
            }
        }

        // Validate parent department relationship
        if (department.getParentDepartment() != null && department.getId() != null) {
            if (!isValidParentDepartment(department.getId(), department.getParentDepartment().getId())) {
                throw new RuntimeException("Invalid parent department relationship - would create circular reference");
            }
        }

        // Validate budget is positive
        if (department.getBudget() != null && department.getBudget().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Department budget cannot be negative");
        }
    }
}