package com.hrm.controller;

import com.hrm.entity.Department;
import com.hrm.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        Optional<Department> department = departmentService.getDepartmentById(id);
        return department.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Department> getDepartmentByName(@PathVariable String name) {
        Optional<Department> department = departmentService.getDepartmentByName(name);
        return department.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        try {
            Department savedDepartment = departmentService.saveDepartment(department);
            return ResponseEntity.ok(savedDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestBody Department departmentDetails) {
        try {
            Department updatedDepartment = departmentService.updateDepartment(id, departmentDetails);
            return ResponseEntity.ok(updatedDepartment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<Department>> getChildDepartments(@PathVariable Long parentId) {
        List<Department> childDepartments = departmentService.getChildDepartments(parentId);
        return ResponseEntity.ok(childDepartments);
    }

    @GetMapping("/root")
    public ResponseEntity<List<Department>> getRootDepartments() {
        List<Department> rootDepartments = departmentService.getRootDepartments();
        return ResponseEntity.ok(rootDepartments);
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<Department>> getDepartmentsByManager(@PathVariable Long managerId) {
        List<Department> departments = departmentService.getDepartmentsByManager(managerId);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Department>> searchDepartments(@RequestParam String searchTerm) {
        List<Department> departments = departmentService.searchDepartments(searchTerm);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}/employee-count")
    public ResponseEntity<Long> countEmployeesInDepartment(@PathVariable Long id) {
        Long count = departmentService.countEmployeesInDepartment(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/name/{name}/exists")
    public ResponseEntity<Boolean> checkDepartmentNameExists(@PathVariable String name) {
        boolean exists = departmentService.isDepartmentNameExists(name);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{id}/hierarchy")
    public ResponseEntity<List<Department>> getDepartmentHierarchy(@PathVariable Long id) {
        List<Department> hierarchy = departmentService.getDepartmentHierarchy(id);
        return ResponseEntity.ok(hierarchy);
    }

    @GetMapping("/{departmentId}/validate-parent/{parentId}")
    public ResponseEntity<Boolean> validateParentDepartment(
            @PathVariable Long departmentId,
            @PathVariable Long parentId) {
        boolean isValid = departmentService.isValidParentDepartment(departmentId, parentId);
        return ResponseEntity.ok(isValid);
    }
}