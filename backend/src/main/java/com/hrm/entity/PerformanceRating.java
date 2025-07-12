package com.hrm.entity;

import com.hrm.entity.enums.RatingCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "performance_ratings")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PerformanceRating extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_review_id", nullable = false)
    @NotNull(message = "Performance review is required")
    @ToString.Exclude
    private PerformanceReview performanceReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating_category", nullable = false)
    @NotNull(message = "Rating category is required")
    private RatingCategory ratingCategory;

    @Column(name = "rating", nullable = false)
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;

    @Column(name = "weight")
    @Min(value = 0, message = "Weight must be at least 0")
    @Max(value = 100, message = "Weight must not exceed 100")
    private Integer weight;

    @Column(name = "comments", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Comments must not exceed 2000 characters")
    private String comments;

    @Column(name = "strengths", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Strengths must not exceed 1000 characters")
    private String strengths;

    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Areas for improvement must not exceed 1000 characters")
    private String areasForImprovement;

    @Column(name = "examples", columnDefinition = "TEXT")
    @Size(max = 1500, message = "Examples must not exceed 1500 characters")
    private String examples;

    @Column(name = "employee_self_rating")
    @Min(value = 1, message = "Employee self rating must be at least 1")
    @Max(value = 5, message = "Employee self rating must not exceed 5")
    private Integer employeeSelfRating;

    @Column(name = "manager_rating")
    @Min(value = 1, message = "Manager rating must be at least 1")
    @Max(value = 5, message = "Manager rating must not exceed 5")
    private Integer managerRating;

    @Column(name = "final_rating")
    @Min(value = 1, message = "Final rating must be at least 1")
    @Max(value = 5, message = "Final rating must not exceed 5")
    private Integer finalRating;

    @Column(name = "is_key_performance_indicator")
    private Boolean isKeyPerformanceIndicator = false;

    @Column(name = "improvement_plan", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Improvement plan must not exceed 1000 characters")
    private String improvementPlan;

    // Convenience methods
    public boolean isExcellent() {
        return rating != null && rating == 5;
    }

    public boolean isGood() {
        return rating != null && rating == 4;
    }

    public boolean isSatisfactory() {
        return rating != null && rating == 3;
    }

    public boolean isNeedsImprovement() {
        return rating != null && rating == 2;
    }

    public boolean isUnsatisfactory() {
        return rating != null && rating == 1;
    }

    public String getRatingDescription() {
        if (rating == null) return "Not rated";
        
        return switch (rating) {
            case 5 -> "Excellent";
            case 4 -> "Good";
            case 3 -> "Satisfactory";
            case 2 -> "Needs Improvement";
            case 1 -> "Unsatisfactory";
            default -> "Invalid rating";
        };
    }

    public boolean hasRatingGap() {
        return employeeSelfRating != null && managerRating != null && 
               Math.abs(employeeSelfRating - managerRating) > 1;
    }

    public double getWeightedScore() {
        if (finalRating == null || weight == null) return 0.0;
        return (finalRating * weight) / 100.0;
    }
}