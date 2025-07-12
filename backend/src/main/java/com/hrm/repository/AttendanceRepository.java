package com.hrm.repository;

import com.hrm.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    List<Attendance> findByEmployeeId(Long employeeId);
    
    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);
    
    Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.workDate BETWEEN :startDate AND :endDate ORDER BY a.workDate DESC")
    List<Attendance> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.employee.department.id = :departmentId " +
           "AND a.workDate = :workDate")
    List<Attendance> findByDepartmentIdAndDate(@Param("departmentId") Long departmentId,
                                              @Param("workDate") LocalDate workDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.employee.manager.id = :managerId " +
           "AND a.workDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByManagerIdAndDateRange(@Param("managerId") Long managerId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(a.hoursWorked) FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.workDate BETWEEN :startDate AND :endDate")
    Double getTotalHoursWorked(@Param("employeeId") Long employeeId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.workDate BETWEEN :startDate AND :endDate AND a.checkInTime IS NOT NULL")
    Long getWorkingDaysCount(@Param("employeeId") Long employeeId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.overtimeHours > 0 " +
           "AND a.workDate BETWEEN :startDate AND :endDate")
    List<Attendance> findOvertimeRecords(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
}