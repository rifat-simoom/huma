package com.hrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hrm.entity.enums.LeaveStatus;
import com.hrm.entity.enums.LeaveType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "leave_requests")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LeaveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    @ToString.Exclude
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column(name = "days_requested", nullable = false)
    @NotNull(message = "Days requested is required")
    private Integer daysRequested;

    @Column(name = "reason", nullable = false)
    @NotNull(message = "Reason is required")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Leave status is required")
    private LeaveStatus status = LeaveStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    @ToString.Exclude
    private Employee approver;

    @Column(name = "approver_comments")
    @Size(max = 1000, message = "Approver comments must not exceed 1000 characters")
    private String approverComments;

    @Column(name = "approved_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDate approvedDate;

    @Column(name = "airflow_dag_run_id")
    private String airflowDagRunId;

    @Column(name = "airflow_task_id")
    private String airflowTaskId;

    @Column(name = "is_half_day")
    private Boolean isHalfDay = false;

    @Column(name = "emergency_contact_notified")
    private Boolean emergencyContactNotified = false;

    @Column(name = "attachments")
    @ElementCollection
    @CollectionTable(name = "leave_request_attachments", joinColumns = @JoinColumn(name = "leave_request_id"))
    @Column(name = "attachment_url")
    private Set<String> attachments = new HashSet<>();

    // Convenience methods
    public boolean isPending() {
        return status == LeaveStatus.PENDING;
    }

    public boolean isApproved() {
        return status == LeaveStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == LeaveStatus.REJECTED;
    }

    public boolean isCancelled() {
        return status == LeaveStatus.CANCELLED;
    }

    public boolean isInProgress() {
        return status == LeaveStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == LeaveStatus.COMPLETED;
    }

    public boolean canBeApproved() {
        return status == LeaveStatus.PENDING;
    }

    public boolean canBeRejected() {
        return status == LeaveStatus.PENDING;
    }

    public boolean canBeCancelled() {
        return status == LeaveStatus.PENDING || status == LeaveStatus.APPROVED;
    }
}