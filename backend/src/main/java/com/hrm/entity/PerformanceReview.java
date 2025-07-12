package com.hrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hrm.entity.enums.ReviewStatus;
import com.hrm.entity.enums.ReviewType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "performance_reviews")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PerformanceReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    @ToString.Exclude
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @NotNull(message = "Reviewer is required")
    @ToString.Exclude
    private Employee reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    @NotNull(message = "Review type is required")
    private ReviewType reviewType;

    @Column(name = "review_period_start", nullable = false)
    @NotNull(message = "Review period start is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reviewPeriodStart;

    @Column(name = "review_period_end", nullable = false)
    @NotNull(message = "Review period end is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reviewPeriodEnd;

    @Column(name = "due_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Column(name = "completed_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate completedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Review status is required")
    private ReviewStatus status = ReviewStatus.DRAFT;

    @Column(name = "overall_rating")
    @Min(value = 1, message = "Overall rating must be at least 1")
    @Max(value = 5, message = "Overall rating must not exceed 5")
    private Integer overallRating;

    @Column(name = "employee_self_assessment", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Employee self assessment must not exceed 5000 characters")
    private String employeeSelfAssessment;

    @Column(name = "manager_feedback", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Manager feedback must not exceed 5000 characters")
    private String managerFeedback;

    @Column(name = "achievements", columnDefinition = "TEXT")
    @Size(max = 3000, message = "Achievements must not exceed 3000 characters")
    private String achievements;

    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    @Size(max = 3000, message = "Areas for improvement must not exceed 3000 characters")
    private String areasForImprovement;

    @Column(name = "goals_for_next_period", columnDefinition = "TEXT")
    @Size(max = 3000, message = "Goals for next period must not exceed 3000 characters")
    private String goalsForNextPeriod;

    @Column(name = "development_plan", columnDefinition = "TEXT")
    @Size(max = 3000, message = "Development plan must not exceed 3000 characters")
    private String developmentPlan;

    @Column(name = "training_recommendations", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Training recommendations must not exceed 2000 characters")
    private String trainingRecommendations;

    @Column(name = "salary_adjustment_recommended")
    private Boolean salaryAdjustmentRecommended = false;

    @Column(name = "promotion_recommended")
    private Boolean promotionRecommended = false;

    @Column(name = "additional_comments", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Additional comments must not exceed 2000 characters")
    private String additionalComments;

    @OneToMany(mappedBy = "performanceReview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PerformanceGoal> performanceGoals = new ArrayList<>();

    @OneToMany(mappedBy = "performanceReview", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PerformanceRating> performanceRatings = new ArrayList<>();

    @Column(name = "is_calibrated")
    private Boolean isCalibrated = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calibrated_by_id")
    @ToString.Exclude
    private Employee calibratedBy;

    @Column(name = "calibration_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate calibrationDate;

    // Convenience methods
    public boolean isDraft() {
        return status == ReviewStatus.DRAFT;
    }

    public boolean isInProgress() {
        return status == ReviewStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == ReviewStatus.COMPLETED;
    }

    public boolean isSubmitted() {
        return status == ReviewStatus.SUBMITTED;
    }

    public boolean isApproved() {
        return status == ReviewStatus.APPROVED;
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !isCompleted();
    }

    public boolean canBeEdited() {
        return status == ReviewStatus.DRAFT || status == ReviewStatus.IN_PROGRESS;
    }

    public boolean canBeSubmitted() {
        return status == ReviewStatus.IN_PROGRESS && overallRating != null;
    }

    public boolean canBeApproved() {
        return status == ReviewStatus.SUBMITTED;
    }

    public String getReviewPeriodDescription() {
        return reviewPeriodStart.toString() + " to " + reviewPeriodEnd.toString();
    }
}