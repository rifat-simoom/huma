package com.hrm.controller;

import com.hrm.entity.Attendance;
import com.hrm.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<List<Attendance>> getAllAttendance() {
        List<Attendance> attendance = attendanceService.getAllAttendance();
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attendance> getAttendanceById(@PathVariable Long id) {
        Optional<Attendance> attendance = attendanceService.getAttendanceById(id);
        return attendance.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Attendance>> getAttendanceByEmployee(@PathVariable Long employeeId) {
        List<Attendance> attendance = attendanceService.getAttendanceByEmployee(employeeId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/employee/{employeeId}/paginated")
    public ResponseEntity<Page<Attendance>> getAttendanceByEmployeePaginated(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "workDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Attendance> attendance = attendanceService.getAttendanceByEmployee(employeeId, pageable);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/employee/{employeeId}/today")
    public ResponseEntity<Attendance> getTodayAttendance(@PathVariable Long employeeId) {
        Optional<Attendance> attendance = attendanceService.getTodayAttendance(employeeId);
        return attendance.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/employee/{employeeId}/check-in")
    public ResponseEntity<Attendance> checkIn(
            @PathVariable Long employeeId,
            @RequestParam(required = false) String location,
            HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            Attendance attendance = attendanceService.checkIn(employeeId, ipAddress, location);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/employee/{employeeId}/check-out")
    public ResponseEntity<Attendance> checkOut(@PathVariable Long employeeId) {
        try {
            Attendance attendance = attendanceService.checkOut(employeeId);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/employee/{employeeId}/start-break")
    public ResponseEntity<Attendance> startBreak(@PathVariable Long employeeId) {
        try {
            Attendance attendance = attendanceService.startBreak(employeeId);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/employee/{employeeId}/end-break")
    public ResponseEntity<Attendance> endBreak(@PathVariable Long employeeId) {
        try {
            Attendance attendance = attendanceService.endBreak(employeeId);
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/employee/{employeeId}/date-range")
    public ResponseEntity<List<Attendance>> getAttendanceByDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Attendance> attendance = attendanceService.getAttendanceByDateRange(employeeId, startDate, endDate);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/department/{departmentId}/date/{date}")
    public ResponseEntity<List<Attendance>> getDepartmentAttendanceForDate(
            @PathVariable Long departmentId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<Attendance> attendance = attendanceService.getDepartmentAttendanceForDate(departmentId, date);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/manager/{managerId}/team")
    public ResponseEntity<List<Attendance>> getManagerTeamAttendance(
            @PathVariable Long managerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Attendance> attendance = attendanceService.getManagerTeamAttendance(managerId, startDate, endDate);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/employee/{employeeId}/total-hours")
    public ResponseEntity<Double> getTotalHoursWorked(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Double totalHours = attendanceService.getTotalHoursWorked(employeeId, startDate, endDate);
        return ResponseEntity.ok(totalHours != null ? totalHours : 0.0);
    }

    @GetMapping("/employee/{employeeId}/working-days")
    public ResponseEntity<Long> getWorkingDaysCount(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Long workingDays = attendanceService.getWorkingDaysCount(employeeId, startDate, endDate);
        return ResponseEntity.ok(workingDays);
    }

    @GetMapping("/overtime")
    public ResponseEntity<List<Attendance>> getOvertimeRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Attendance> overtimeRecords = attendanceService.getOvertimeRecords(startDate, endDate);
        return ResponseEntity.ok(overtimeRecords);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Attendance> updateAttendance(@PathVariable Long id, @RequestBody Attendance attendanceDetails) {
        try {
            Attendance updatedAttendance = attendanceService.updateAttendance(id, attendanceDetails);
            return ResponseEntity.ok(updatedAttendance);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        try {
            attendanceService.deleteAttendance(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/employee/{employeeId}/status/checked-in")
    public ResponseEntity<Boolean> hasCheckedInToday(@PathVariable Long employeeId) {
        boolean checkedIn = attendanceService.hasCheckedInToday(employeeId);
        return ResponseEntity.ok(checkedIn);
    }

    @GetMapping("/employee/{employeeId}/status/checked-out")
    public ResponseEntity<Boolean> hasCheckedOutToday(@PathVariable Long employeeId) {
        boolean checkedOut = attendanceService.hasCheckedOutToday(employeeId);
        return ResponseEntity.ok(checkedOut);
    }

    @GetMapping("/employee/{employeeId}/status/on-break")
    public ResponseEntity<Boolean> isOnBreak(@PathVariable Long employeeId) {
        boolean onBreak = attendanceService.isOnBreak(employeeId);
        return ResponseEntity.ok(onBreak);
    }

    @PostMapping
    public ResponseEntity<Attendance> createAttendance(@RequestBody Attendance attendance) {
        try {
            Attendance savedAttendance = attendanceService.saveAttendance(attendance);
            return ResponseEntity.ok(savedAttendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}