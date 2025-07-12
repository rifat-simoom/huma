package com.hrm.repository;

import com.hrm.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByName(String name);
    
    List<Department> findByParentDepartmentId(Long parentDepartmentId);
    
    @Query("SELECT d FROM Department d WHERE d.parentDepartment IS NULL")
    List<Department> findRootDepartments();
    
    @Query("SELECT d FROM Department d WHERE d.managerId = :managerId")
    List<Department> findByManagerId(@Param("managerId") Long managerId);
    
    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Department> searchDepartments(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId")
    Long countEmployeesInDepartment(@Param("departmentId") Long departmentId);
}