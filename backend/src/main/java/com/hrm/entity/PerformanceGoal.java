package com.hrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hrm.entity.enums.GoalStatus;
import com.hrm.entity.enums.GoalType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "performance_goals")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PerformanceGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_review_id", nullable = false)
    @NotNull(message = "Performance review is required")
    @ToString.Exclude
    private PerformanceReview performanceReview;

    @Column(name = "title", nullable = false)
    @NotNull(message = "Goal title is required")
    @Size(max = 255, message = "Goal title must not exceed 255 characters")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Goal description must not exceed 2000 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    @NotNull(message = "Goal type is required")
    private GoalType goalType;

    @Column(name = "target_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @Column(name = "completion_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate completionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Goal status is required")
    private GoalStatus status = GoalStatus.NOT_STARTED;

    @Column(name = "progress_percentage")
    @Min(value = 0, message = "Progress percentage must be at least 0")
    @Max(value = 100, message = "Progress percentage must not exceed 100")
    private Integer progressPercentage = 0;

    @Column(name = "weight")
    @Min(value = 0, message = "Weight must be at least 0")
    @Max(value = 100, message = "Weight must not exceed 100")
    private Integer weight;

    @Column(name = "achievement_rating")
    @Min(value = 1, message = "Achievement rating must be at least 1")
    @Max(value = 5, message = "Achievement rating must not exceed 5")
    private Integer achievementRating;

    @Column(name = "measurable_criteria", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Measurable criteria must not exceed 1000 characters")
    private String measurableCriteria;

    @Column(name = "actual_result", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Actual result must not exceed 1000 characters")
    private String actualResult;

    @Column(name = "manager_comments", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Manager comments must not exceed 1000 characters")
    private String managerComments;

    @Column(name = "employee_comments", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Employee comments must not exceed 1000 characters")
    private String employeeComments;

    @Column(name = "is_carry_forward")
    private Boolean isCarryForward = false;

    @Column(name = "priority")
    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 5, message = "Priority must not exceed 5")
    private Integer priority;

    // Convenience methods
    public boolean isCompleted() {
        return status == GoalStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return status == GoalStatus.IN_PROGRESS;
    }

    public boolean isNotStarted() {
        return status == GoalStatus.NOT_STARTED;
    }

    public boolean isOnHold() {
        return status == GoalStatus.ON_HOLD;
    }

    public boolean isCancelled() {
        return status == GoalStatus.CANCELLED;
    }

    public boolean isOverdue() {
        return targetDate != null && targetDate.isBefore(LocalDate.now()) && !isCompleted();
    }

    public boolean canBeCompleted() {
        return status == GoalStatus.IN_PROGRESS && progressPercentage != null && progressPercentage == 100;
    }

    public String getProgressDescription() {
        if (progressPercentage == null) {
            return "Not started";
        }
        return progressPercentage + "% complete";
    }
}