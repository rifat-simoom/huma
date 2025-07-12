package com.hrm.service;

import com.hrm.entity.Employee;
import com.hrm.entity.LeaveRequest;
import com.hrm.entity.enums.LeaveStatus;
import com.hrm.repository.EmployeeRepository;
import com.hrm.repository.LeaveRequestRepository;
import com.hrm.service.dto.LeaveRequestCreateDTO;
import com.hrm.service.dto.LeaveRequestDTO;
import com.hrm.service.dto.LeaveRequestUpdateDTO;
import com.hrm.service.mapper.LeaveRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final WebClient webClient;
    
    @Value("${airflow.base-url}")
    private String airflowBaseUrl;
    
    @Value("${airflow.username}")
    private String airflowUsername;
    
    @Value("${airflow.password}")
    private String airflowPassword;
    
    @Value("${airflow.dag-id}")
    private String airflowDagId;

    public LeaveRequestDTO createLeaveRequest(LeaveRequestCreateDTO createDTO) {
        log.info("Creating leave request for employee: {}", createDTO.getEmployeeId());
        
        Employee employee = employeeRepository.findById(createDTO.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Validate leave request
        validateLeaveRequest(createDTO, employee);
        
        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(createDTO);
        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus(LeaveStatus.PENDING);
        
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        // Trigger Airflow workflow
        triggerAirflowWorkflow(savedRequest);
        
        log.info("Leave request created with ID: {}", savedRequest.getId());
        return leaveRequestMapper.toDTO(savedRequest);
    }

    public LeaveRequestDTO updateLeaveRequest(Long id, LeaveRequestUpdateDTO updateDTO) {
        log.info("Updating leave request: {}", id);
        
        LeaveRequest existingRequest = leaveRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        
        // Only allow updates if request is pending
        if (existingRequest.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Cannot update leave request with status: " + existingRequest.getStatus());
        }
        
        leaveRequestMapper.updateEntity(updateDTO, existingRequest);
        LeaveRequest updatedRequest = leaveRequestRepository.save(existingRequest);
        
        log.info("Leave request updated: {}", id);
        return leaveRequestMapper.toDTO(updatedRequest);
    }

    public LeaveRequestDTO approveLeaveRequest(Long id, String approverComments) {
        log.info("Approving leave request: {}", id);
        
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        
        if (!leaveRequest.canBeApproved()) {
            throw new RuntimeException("Leave request cannot be approved in current status: " + leaveRequest.getStatus());
        }
        
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApproverComments(approverComments);
        leaveRequest.setApprovedDate(LocalDate.now());
        
        // Update employee leave balance
        updateEmployeeLeaveBalance(leaveRequest);
        
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        log.info("Leave request approved: {}", id);
        return leaveRequestMapper.toDTO(savedRequest);
    }

    public LeaveRequestDTO rejectLeaveRequest(Long id, String approverComments) {
        log.info("Rejecting leave request: {}", id);
        
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        
        if (!leaveRequest.canBeRejected()) {
            throw new RuntimeException("Leave request cannot be rejected in current status: " + leaveRequest.getStatus());
        }
        
        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApproverComments(approverComments);
        
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        log.info("Leave request rejected: {}", id);
        return leaveRequestMapper.toDTO(savedRequest);
    }

    public LeaveRequestDTO cancelLeaveRequest(Long id) {
        log.info("Cancelling leave request: {}", id);
        
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        
        if (!leaveRequest.canBeCancelled()) {
            throw new RuntimeException("Leave request cannot be cancelled in current status: " + leaveRequest.getStatus());
        }
        
        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        
        // If request was approved, restore employee leave balance
        if (leaveRequest.getStatus() == LeaveStatus.APPROVED) {
            restoreEmployeeLeaveBalance(leaveRequest);
        }
        
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        log.info("Leave request cancelled: {}", id);
        return leaveRequestMapper.toDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public Optional<LeaveRequestDTO> getLeaveRequest(Long id) {
        return leaveRequestRepository.findById(id)
            .map(leaveRequestMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getLeaveRequests(Pageable pageable) {
        return leaveRequestRepository.findAll(pageable)
            .map(leaveRequestMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getLeaveRequestsByEmployee(Long employeeId, Pageable pageable) {
        return leaveRequestRepository.findByEmployeeId(employeeId, pageable)
            .map(leaveRequestMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getLeaveRequestsByStatus(LeaveStatus status, Pageable pageable) {
        return leaveRequestRepository.findByStatus(status, pageable)
            .map(leaveRequestMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getLeaveRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        return leaveRequestRepository.findByDateRange(startDate, endDate)
            .stream()
            .map(leaveRequestMapper::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDTO> getPendingLeaveRequestsForManager(Long managerId, Pageable pageable) {
        return leaveRequestRepository.findPendingRequestsForManager(managerId, pageable)
            .map(leaveRequestMapper::toDTO);
    }

    private void validateLeaveRequest(LeaveRequestCreateDTO createDTO, Employee employee) {
        // Check if employee has sufficient leave balance
        switch (createDTO.getLeaveType()) {
            case ANNUAL:
                if (employee.getAnnualLeaveBalance() < createDTO.getDaysRequested()) {
                    throw new RuntimeException("Insufficient annual leave balance");
                }
                break;
            case SICK:
                if (employee.getSickLeaveBalance() < createDTO.getDaysRequested()) {
                    throw new RuntimeException("Insufficient sick leave balance");
                }
                break;
            default:
                // Other leave types might not require balance check
                break;
        }
        
        // Check for overlapping leave requests
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingRequests(
            employee.getId(), createDTO.getStartDate(), createDTO.getEndDate()
        );
        
        if (!overlappingRequests.isEmpty()) {
            throw new RuntimeException("Overlapping leave requests found");
        }
        
        // Check if start date is in the past
        if (createDTO.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Leave start date cannot be in the past");
        }
        
        // Check if end date is before start date
        if (createDTO.getEndDate().isBefore(createDTO.getStartDate())) {
            throw new RuntimeException("Leave end date cannot be before start date");
        }
    }

    private void updateEmployeeLeaveBalance(LeaveRequest leaveRequest) {
        Employee employee = leaveRequest.getEmployee();
        
        switch (leaveRequest.getLeaveType()) {
            case ANNUAL:
                employee.setAnnualLeaveBalance(employee.getAnnualLeaveBalance() - leaveRequest.getDaysRequested());
                break;
            case SICK:
                employee.setSickLeaveBalance(employee.getSickLeaveBalance() - leaveRequest.getDaysRequested());
                break;
            default:
                // Other leave types might not affect balance
                break;
        }
        
        employeeRepository.save(employee);
    }

    private void restoreEmployeeLeaveBalance(LeaveRequest leaveRequest) {
        Employee employee = leaveRequest.getEmployee();
        
        switch (leaveRequest.getLeaveType()) {
            case ANNUAL:
                employee.setAnnualLeaveBalance(employee.getAnnualLeaveBalance() + leaveRequest.getDaysRequested());
                break;
            case SICK:
                employee.setSickLeaveBalance(employee.getSickLeaveBalance() + leaveRequest.getDaysRequested());
                break;
            default:
                // Other leave types might not affect balance
                break;
        }
        
        employeeRepository.save(employee);
    }

    private void triggerAirflowWorkflow(LeaveRequest leaveRequest) {
        try {
            Map<String, Object> dagParams = new HashMap<>();
            dagParams.put("leave_request_id", leaveRequest.getId().toString());
            dagParams.put("employee_id", leaveRequest.getEmployee().getId().toString());
            dagParams.put("manager_id", leaveRequest.getEmployee().getManager() != null ? 
                leaveRequest.getEmployee().getManager().getId().toString() : "");
            dagParams.put("leave_type", leaveRequest.getLeaveType().toString());
            dagParams.put("days_requested", leaveRequest.getDaysRequested());
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dag_run_id", "leave_request_" + leaveRequest.getId() + "_" + System.currentTimeMillis());
            requestBody.put("conf", dagParams);
            
            String airflowUrl = airflowBaseUrl + "/api/v1/dags/" + airflowDagId + "/dagRuns";
            
            Mono<String> response = webClient.post()
                .uri(airflowUrl)
                .headers(headers -> headers.setBasicAuth(airflowUsername, airflowPassword))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);
            
            response.subscribe(
                result -> {
                    log.info("Airflow workflow triggered successfully for leave request: {}", leaveRequest.getId());
                    leaveRequest.setAirflowDagRunId(((Map<String, Object>) requestBody).get("dag_run_id").toString());
                    leaveRequestRepository.save(leaveRequest);
                },
                error -> log.error("Failed to trigger Airflow workflow for leave request: {}", leaveRequest.getId(), error)
            );
            
        } catch (Exception e) {
            log.error("Error triggering Airflow workflow for leave request: {}", leaveRequest.getId(), e);
        }
    }
}