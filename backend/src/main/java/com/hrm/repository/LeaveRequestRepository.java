package com.hrm.repository;

import com.hrm.entity.LeaveRequest;
import com.hrm.entity.enums.LeaveStatus;
import com.hrm.entity.enums.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.manager.id = :managerId")
    List<LeaveRequest> findByManagerId(@Param("managerId") Long managerId);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.manager.id = :managerId AND lr.status = :status")
    List<LeaveRequest> findByManagerIdAndStatus(@Param("managerId") Long managerId, @Param("status") LeaveStatus status);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.department.id = :departmentId")
    Page<LeaveRequest> findByDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.status IN ('APPROVED', 'PENDING') " +
           "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaves(@Param("employeeId") Long employeeId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(lr.totalDays) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.leaveType = :leaveType AND lr.status = 'APPROVED' " +
           "AND YEAR(lr.startDate) = :year")
    Double getTotalApprovedLeavesByTypeAndYear(@Param("employeeId") Long employeeId,
                                             @Param("leaveType") LeaveType leaveType,
                                             @Param("year") int year);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = :status " +
           "AND lr.startDate <= :date ORDER BY lr.startDate ASC")
    List<LeaveRequest> findLeaveRequestsByStatusAndDate(@Param("status") LeaveStatus status,
                                                       @Param("date") LocalDate date);
}