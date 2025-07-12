package com.hrm.repository;

import com.hrm.entity.PerformanceReview;
import com.hrm.entity.enums.PerformanceStatus;
import com.hrm.entity.enums.ReviewPeriod;
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
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    
    List<PerformanceReview> findByEmployeeId(Long employeeId);
    
    Page<PerformanceReview> findByEmployeeId(Long employeeId, Pageable pageable);
    
    List<PerformanceReview> findByEmployeeIdAndStatus(Long employeeId, PerformanceStatus status);
    
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.employee.manager.id = :managerId")
    List<PerformanceReview> findByManagerId(@Param("managerId") Long managerId);
    
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.employee.manager.id = :managerId AND pr.status = :status")
    List<PerformanceReview> findByManagerIdAndStatus(@Param("managerId") Long managerId, 
                                                    @Param("status") PerformanceStatus status);
    
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.employee.department.id = :departmentId")
    Page<PerformanceReview> findByDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);
    
    Optional<PerformanceReview> findByEmployeeIdAndReviewPeriodAndReviewYear(Long employeeId, 
                                                                            ReviewPeriod reviewPeriod, 
                                                                            int reviewYear);
    
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.reviewPeriod = :reviewPeriod " +
           "AND pr.reviewYear = :reviewYear AND pr.status = :status")
    List<PerformanceReview> findByReviewPeriodAndYearAndStatus(@Param("reviewPeriod") ReviewPeriod reviewPeriod,
                                                              @Param("reviewYear") int reviewYear,
                                                              @Param("status") PerformanceStatus status);
    
    @Query("SELECT pr FROM PerformanceReview pr WHERE pr.status = 'PENDING_MANAGER_REVIEW' " +
           "AND pr.dueDate <= :dueDate ORDER BY pr.dueDate ASC")
    List<PerformanceReview> findOverdueReviews(@Param("dueDate") LocalDate dueDate);
    
    @Query("SELECT AVG(pr.overallRating) FROM PerformanceReview pr WHERE pr.employee.id = :employeeId " +
           "AND pr.status = 'COMPLETED'")
    Double getAverageRatingForEmployee(@Param("employeeId") Long employeeId);
}