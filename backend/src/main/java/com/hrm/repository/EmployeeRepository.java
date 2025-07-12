package com.hrm.repository;

import com.hrm.entity.Employee;
import com.hrm.entity.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmail(String email);
    
    Optional<Employee> findByEmployeeId(String employeeId);
    
    List<Employee> findByDepartmentId(Long departmentId);
    
    List<Employee> findByManagerId(Long managerId);
    
    List<Employee> findByStatus(EmployeeStatus status);
    
    @Query("SELECT e FROM Employee e WHERE e.department.id = :departmentId AND e.status = :status")
    List<Employee> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId, 
                                              @Param("status") EmployeeStatus status);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Employee> searchEmployees(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.status = 'ACTIVE'")
    Long countActiveEmployeesByDepartment(@Param("departmentId") Long departmentId);
    
    @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId AND e.status = 'ACTIVE'")
    List<Employee> findActiveDirectReports(@Param("managerId") Long managerId);
}