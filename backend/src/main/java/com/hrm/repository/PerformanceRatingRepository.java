package com.hrm.repository;

import com.hrm.entity.PerformanceRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceRatingRepository extends JpaRepository<PerformanceRating, Long> {
    
    List<PerformanceRating> findByPerformanceReviewId(Long performanceReviewId);
    
    List<PerformanceRating> findByPerformanceReviewIdAndCategory(Long performanceReviewId, String category);
    
    @Query("SELECT pr FROM PerformanceRating pr WHERE pr.performanceReview.employee.id = :employeeId")
    List<PerformanceRating> findByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT AVG(pr.rating) FROM PerformanceRating pr WHERE pr.performanceReview.employee.id = :employeeId " +
           "AND pr.category = :category")
    Double getAverageRatingByEmployeeAndCategory(@Param("employeeId") Long employeeId, 
                                                @Param("category") String category);
    
    @Query("SELECT pr.category, AVG(pr.rating) FROM PerformanceRating pr " +
           "WHERE pr.performanceReview.employee.department.id = :departmentId " +
           "GROUP BY pr.category")
    List<Object[]> getAverageRatingsByDepartmentAndCategory(@Param("departmentId") Long departmentId);
    
    @Query("SELECT COUNT(pr) FROM PerformanceRating pr WHERE pr.performanceReview.id = :reviewId")
    Long countRatingsByReview(@Param("reviewId") Long reviewId);
}