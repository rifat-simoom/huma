package com.hrm.service.mapper;

import com.hrm.entity.LeaveRequest;
import com.hrm.service.dto.LeaveRequestCreateDTO;
import com.hrm.service.dto.LeaveRequestDTO;
import com.hrm.service.dto.LeaveRequestUpdateDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class LeaveRequestMapper {

    public LeaveRequestDTO toDTO(LeaveRequest entity) {
        if (entity == null) {
            return null;
        }

        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(entity.getId());
        dto.setEmployeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null);
        dto.setEmployeeName(entity.getEmployee() != null ? 
            entity.getEmployee().getFirstName() + " " + entity.getEmployee().getLastName() : null);
        dto.setLeaveType(entity.getLeaveType());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setTotalDays(entity.getTotalDays());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());
        dto.setApproverComments(entity.getApproverComments());
        dto.setAppliedDate(entity.getAppliedDate());
        dto.setApprovedDate(entity.getApprovedDate());
        dto.setWorkflowInstanceId(entity.getWorkflowInstanceId());

        return dto;
    }

    public LeaveRequest toEntity(LeaveRequestCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        LeaveRequest entity = new LeaveRequest();
        entity.setLeaveType(dto.getLeaveType());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setReason(dto.getReason());
        entity.setAppliedDate(LocalDate.now());
        
        // Calculate total days
        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            long daysBetween = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
            entity.setTotalDays((double) daysBetween);
        }

        return entity;
    }

    public void updateEntity(LeaveRequestUpdateDTO dto, LeaveRequest entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getLeaveType() != null) {
            entity.setLeaveType(dto.getLeaveType());
        }
        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            entity.setEndDate(dto.getEndDate());
        }
        if (dto.getReason() != null) {
            entity.setReason(dto.getReason());
        }

        // Recalculate total days if dates changed
        if (entity.getStartDate() != null && entity.getEndDate() != null) {
            long daysBetween = ChronoUnit.DAYS.between(entity.getStartDate(), entity.getEndDate()) + 1;
            entity.setTotalDays((double) daysBetween);
        }
    }
}