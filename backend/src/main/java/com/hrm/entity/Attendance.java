package com.hrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hrm.entity.enums.AttendanceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "date"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    @ToString.Exclude
    private Employee employee;

    @Column(name = "date", nullable = false)
    @NotNull(message = "Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Column(name = "check_in_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;

    @Column(name = "break_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime breakStartTime;

    @Column(name = "break_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime breakEndTime;

    @Column(name = "total_hours_worked")
    private Double totalHoursWorked;

    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    @Column(name = "is_overtime")
    private Boolean isOvertime = false;

    @Column(name = "overtime_hours")
    private Double overtimeHours;

    @Column(name = "notes")
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Column(name = "location")
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "is_remote")
    private Boolean isRemote = false;

    @Column(name = "approved_by_manager")
    private Boolean approvedByManager = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    @ToString.Exclude
    private Employee approvedBy;

    // Convenience methods
    public boolean hasCheckedIn() {
        return checkInTime != null;
    }

    public boolean hasCheckedOut() {
        return checkOutTime != null;
    }

    public boolean isOnBreak() {
        return breakStartTime != null && breakEndTime == null;
    }

    public boolean hasCompletedBreak() {
        return breakStartTime != null && breakEndTime != null;
    }

    public long getWorkingMinutes() {
        if (checkInTime == null || checkOutTime == null) {
            return 0;
        }
        
        long totalMinutes = ChronoUnit.MINUTES.between(checkInTime, checkOutTime);
        
        if (hasCompletedBreak()) {
            totalMinutes -= breakDurationMinutes != null ? breakDurationMinutes : 0;
        }
        
        return totalMinutes;
    }

    public double getWorkingHours() {
        return getWorkingMinutes() / 60.0;
    }

    public boolean isLateArrival() {
        if (checkInTime == null) return false;
        // Assuming standard work start time is 9:00 AM
        return checkInTime.getHour() > 9 || (checkInTime.getHour() == 9 && checkInTime.getMinute() > 0);
    }

    public boolean isEarlyDeparture() {
        if (checkOutTime == null) return false;
        // Assuming standard work end time is 5:00 PM
        return checkOutTime.getHour() < 17;
    }

    public boolean isFullDay() {
        return status == AttendanceStatus.PRESENT && getWorkingHours() >= 8.0;
    }

    public boolean isHalfDay() {
        return status == AttendanceStatus.HALF_DAY;
    }
}