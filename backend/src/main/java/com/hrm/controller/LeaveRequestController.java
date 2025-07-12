package com.hrm.controller;

import com.hrm.entity.LeaveRequest;
import com.hrm.entity.enums.LeaveStatus;
import com.hrm.entity.enums.LeaveType;
import com.hrm.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/leave-requests")
@CrossOrigin(origins = "*")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @GetMapping
    public ResponseEntity<List<LeaveRequest>> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(leaveRequests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        Optional<LeaveRequest> leaveRequest = leaveRequestService.getLeaveRequestById(id);
        return leaveRequest.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByEmployee(@PathVariable Long employeeId) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByEmployee(employeeId);
        return ResponseEntity.ok(leaveRequests);
    }

    @GetMapping("/employee/{employeeId}/status/{status}")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByEmployeeAndStatus(
            @PathVariable Long employeeId,
            @PathVariable LeaveStatus status) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByEmployeeAndStatus(employeeId, status);
        return ResponseEntity.ok(leaveRequests);
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByManager(@PathVariable Long managerId) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByManager(managerId);
        return ResponseEntity.ok(leaveRequests);
    }

    @GetMapping("/manager/{managerId}/status/{status}")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByManagerAndStatus(
            @PathVariable Long managerId,
            @PathVariable LeaveStatus status) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByManagerAndStatus(managerId, status);
        return ResponseEntity.ok(leaveRequests);
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<Page<LeaveRequest>> getLeaveRequestsByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByDepartment(departmentId, pageable);
        return ResponseEntity.ok(leaveRequests);
    }

    @PostMapping
    public ResponseEntity<LeaveRequest> createLeaveRequest(@RequestBody LeaveRequest leaveRequest) {
        try {
            LeaveRequest savedLeaveRequest = leaveRequestService.saveLeaveRequest(leaveRequest);
            return ResponseEntity.ok(savedLeaveRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequest> updateLeaveRequest(@PathVariable Long id, @RequestBody LeaveRequest leaveRequestDetails) {
        try {
            LeaveRequest updatedLeaveRequest = leaveRequestService.updateLeaveRequest(id, leaveRequestDetails);
            return ResponseEntity.ok(updatedLeaveRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveRequest(@PathVariable Long id) {
        try {
            leaveRequestService.deleteLeaveRequest(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<LeaveRequest> approveLeaveRequest(@PathVariable Long id, @RequestParam String approverComments) {
        try {
            LeaveRequest approvedRequest = leaveRequestService.approveLeaveRequest(id, approverComments);
            return ResponseEntity.ok(approvedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<LeaveRequest> rejectLeaveRequest(@PathVariable Long id, @RequestParam String rejectionReason) {
        try {
            LeaveRequest rejectedRequest = leaveRequestService.rejectLeaveRequest(id, rejectionReason);
            return ResponseEntity.ok(rejectedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<LeaveRequest> cancelLeaveRequest(@PathVariable Long id) {
        try {
            LeaveRequest cancelledRequest = leaveRequestService.cancelLeaveRequest(id);
            return ResponseEntity.ok(cancelledRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/employee/{employeeId}/overlapping")
    public ResponseEntity<List<LeaveRequest>> checkOverlappingLeaves(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<LeaveRequest> overlappingLeaves = leaveRequestService.getOverlappingLeaves(employeeId, startDate, endDate);
        return ResponseEntity.ok(overlappingLeaves);
    }

    @GetMapping("/employee/{employeeId}/balance/{leaveType}/{year}")
    public ResponseEntity<Double> getLeaveBalance(
            @PathVariable Long employeeId,
            @PathVariable LeaveType leaveType,
            @PathVariable int year) {
        
        Double balance = leaveRequestService.getLeaveBalance(employeeId, leaveType, year);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/status/{status}/date/{date}")
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByStatusAndDate(
            @PathVariable LeaveStatus status,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByStatusAndDate(status, date);
        return ResponseEntity.ok(leaveRequests);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<LeaveRequest> submitLeaveRequest(@PathVariable Long id) {
        try {
            LeaveRequest submittedRequest = leaveRequestService.submitLeaveRequest(id);
            return ResponseEntity.ok(submittedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}