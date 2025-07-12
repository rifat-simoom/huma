package com.hrm.repository;

import com.hrm.entity.PerformanceGoal;
import com.hrm.entity.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PerformanceGoalRepository extends JpaRepository<PerformanceGoal, Long> {
    
    List<PerformanceGoal> findByEmployeeId(Long employeeId);
    
    List<PerformanceGoal> findByEmployeeIdAndStatus(Long employeeId, GoalStatus status);
    
    List<PerformanceGoal> findByPerformanceReviewId(Long performanceReviewId);
    
    @Query("SELECT pg FROM PerformanceGoal pg WHERE pg.employee.manager.id = :managerId")
    List<PerformanceGoal> findByManagerId(@Param("managerId") Long managerId);
    
    @Query("SELECT pg FROM PerformanceGoal pg WHERE pg.employee.department.id = :departmentId")
    List<PerformanceGoal> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT pg FROM PerformanceGoal pg WHERE pg.targetDate <= :date AND pg.status = 'IN_PROGRESS'")
    List<PerformanceGoal> findOverdueGoals(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(pg) FROM PerformanceGoal pg WHERE pg.employee.id = :employeeId AND pg.status = :status")
    Long countGoalsByEmployeeAndStatus(@Param("employeeId") Long employeeId, @Param("status") GoalStatus status);
    
    @Query("SELECT AVG(pg.progressPercentage) FROM PerformanceGoal pg WHERE pg.employee.id = :employeeId " +
           "AND pg.status IN ('IN_PROGRESS', 'COMPLETED')")
    Double getAverageProgressForEmployee(@Param("employeeId") Long employeeId);
}