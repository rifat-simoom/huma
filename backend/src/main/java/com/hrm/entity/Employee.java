package com.hrm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hrm.entity.enums.EmployeeStatus;
import com.hrm.entity.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Employee extends BaseEntity {

    @Column(name = "employee_id", unique = true, nullable = false)
    @NotBlank(message = "Employee ID is required")
    @Size(max = 20, message = "Employee ID must not exceed 20 characters")
    private String employeeId;

    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Column(name = "phone_number")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Column(name = "date_of_birth")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "hire_date", nullable = false)
    @NotNull(message = "Hire date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @Column(name = "termination_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate terminationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Employee status is required")
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "job_title", nullable = false)
    @NotBlank(message = "Job title is required")
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @ToString.Exclude
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @ToString.Exclude
    private Employee manager;

    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private Set<Employee> directReports = new HashSet<>();

    @Column(name = "salary", precision = 10, scale = 2)
    private BigDecimal salary;

    @Column(name = "address")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Column(name = "city")
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Column(name = "state")
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @Column(name = "postal_code")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Column(name = "country")
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    @Column(name = "emergency_contact_name")
    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    @Size(max = 20, message = "Emergency contact phone must not exceed 20 characters")
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relationship")
    @Size(max = 50, message = "Emergency contact relationship must not exceed 50 characters")
    private String emergencyContactRelationship;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "keycloak_user_id")
    private String keycloakUserId;

    @Column(name = "annual_leave_balance")
    private Integer annualLeaveBalance = 20;

    @Column(name = "sick_leave_balance")
    private Integer sickLeaveBalance = 10;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private Set<LeaveRequest> leaveRequests = new HashSet<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private Set<Attendance> attendanceRecords = new HashSet<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private Set<PerformanceReview> performanceReviews = new HashSet<>();

    // Convenience methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isManager() {
        return directReports != null && !directReports.isEmpty();
    }

    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }
}