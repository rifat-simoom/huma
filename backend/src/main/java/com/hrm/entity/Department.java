package com.hrm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "departments")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Department extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    private String name;

    @Column(name = "description")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Column(name = "code", unique = true, nullable = false)
    @NotBlank(message = "Department code is required")
    @Size(max = 20, message = "Department code must not exceed 20 characters")
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_id")
    @ToString.Exclude
    private Employee head;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    @ToString.Exclude
    private Department parentDepartment;

    @OneToMany(mappedBy = "parentDepartment", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private Set<Department> subDepartments = new HashSet<>();

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    private Set<Employee> employees = new HashSet<>();

    @Column(name = "location")
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @Column(name = "budget")
    private Long budget;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Convenience methods
    public int getEmployeeCount() {
        return employees != null ? employees.size() : 0;
    }

    public boolean hasSubDepartments() {
        return subDepartments != null && !subDepartments.isEmpty();
    }

    public boolean isRootDepartment() {
        return parentDepartment == null;
    }
}