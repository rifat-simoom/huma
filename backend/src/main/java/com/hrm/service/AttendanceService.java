package com.hrm.service;

import com.hrm.entity.Attendance;
import com.hrm.entity.Employee;
import com.hrm.repository.AttendanceRepository;
import com.hrm.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceRepository.findById(id);
    }

    public List<Attendance> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    public Page<Attendance> getAttendanceByEmployee(Long employeeId, Pageable pageable) {
        return attendanceRepository.findByEmployeeId(employeeId, pageable);
    }

    public Optional<Attendance> getTodayAttendance(Long employeeId) {
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now());
    }

    public Attendance checkIn(Long employeeId, String ipAddress, String location) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        LocalDate today = LocalDate.now();
        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today);

        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            if (attendance.getCheckInTime() != null) {
                throw new RuntimeException("Employee has already checked in today");
            }
            attendance.setCheckInTime(LocalTime.now());
            attendance.setIpAddress(ipAddress);
            attendance.setLocation(location);
            return attendanceRepository.save(attendance);
        } else {
            Attendance attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setWorkDate(today);
            attendance.setCheckInTime(LocalTime.now());
            attendance.setIpAddress(ipAddress);
            attendance.setLocation(location);
            return attendanceRepository.save(attendance);
        }
    }

    public Attendance checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No check-in record found for today"));

        if (attendance.getCheckInTime() == null) {
            throw new RuntimeException("Employee has not checked in today");
        }

        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Employee has already checked out today");
        }

        LocalTime checkOutTime = LocalTime.now();
        attendance.setCheckOutTime(checkOutTime);

        // Calculate hours worked
        double hoursWorked = calculateHoursWorked(attendance.getCheckInTime(), checkOutTime, attendance.getBreakDuration());
        attendance.setHoursWorked(hoursWorked);

        // Calculate overtime (assuming 8 hours is standard work day)
        double overtimeHours = Math.max(0, hoursWorked - 8.0);
        attendance.setOvertimeHours(overtimeHours);

        return attendanceRepository.save(attendance);
    }

    public Attendance startBreak(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No check-in record found for today"));

        if (attendance.getCheckInTime() == null) {
            throw new RuntimeException("Employee has not checked in today");
        }

        if (attendance.getBreakStartTime() != null) {
            throw new RuntimeException("Break has already started");
        }

        attendance.setBreakStartTime(LocalTime.now());
        return attendanceRepository.save(attendance);
    }

    public Attendance endBreak(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No check-in record found for today"));

        if (attendance.getBreakStartTime() == null) {
            throw new RuntimeException("Break has not been started");
        }

        if (attendance.getBreakEndTime() != null) {
            throw new RuntimeException("Break has already ended");
        }

        LocalTime breakEndTime = LocalTime.now();
        attendance.setBreakEndTime(breakEndTime);

        // Calculate break duration
        double breakDuration = Duration.between(attendance.getBreakStartTime(), breakEndTime).toMinutes() / 60.0;
        attendance.setBreakDuration(breakDuration);

        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getAttendanceByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
    }

    public List<Attendance> getDepartmentAttendanceForDate(Long departmentId, LocalDate date) {
        return attendanceRepository.findByDepartmentIdAndDate(departmentId, date);
    }

    public List<Attendance> getManagerTeamAttendance(Long managerId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByManagerIdAndDateRange(managerId, startDate, endDate);
    }

    public Double getTotalHoursWorked(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.getTotalHoursWorked(employeeId, startDate, endDate);
    }

    public Long getWorkingDaysCount(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.getWorkingDaysCount(employeeId, startDate, endDate);
    }

    public List<Attendance> getOvertimeRecords(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findOvertimeRecords(startDate, endDate);
    }

    public Attendance updateAttendance(Long id, Attendance attendanceDetails) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found with id: " + id));

        attendance.setCheckInTime(attendanceDetails.getCheckInTime());
        attendance.setCheckOutTime(attendanceDetails.getCheckOutTime());
        attendance.setBreakStartTime(attendanceDetails.getBreakStartTime());
        attendance.setBreakEndTime(attendanceDetails.getBreakEndTime());
        attendance.setBreakDuration(attendanceDetails.getBreakDuration());
        attendance.setLocation(attendanceDetails.getLocation());
        attendance.setNotes(attendanceDetails.getNotes());

        // Recalculate hours worked if check-in and check-out times are set
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            double hoursWorked = calculateHoursWorked(attendance.getCheckInTime(), 
                                                    attendance.getCheckOutTime(), 
                                                    attendance.getBreakDuration());
            attendance.setHoursWorked(hoursWorked);
            
            double overtimeHours = Math.max(0, hoursWorked - 8.0);
            attendance.setOvertimeHours(overtimeHours);
        }

        return attendanceRepository.save(attendance);
    }

    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }

    public boolean hasCheckedInToday(Long employeeId) {
        Optional<Attendance> attendance = getTodayAttendance(employeeId);
        return attendance.isPresent() && attendance.get().getCheckInTime() != null;
    }

    public boolean hasCheckedOutToday(Long employeeId) {
        Optional<Attendance> attendance = getTodayAttendance(employeeId);
        return attendance.isPresent() && attendance.get().getCheckOutTime() != null;
    }

    public boolean isOnBreak(Long employeeId) {
        Optional<Attendance> attendance = getTodayAttendance(employeeId);
        return attendance.isPresent() && 
               attendance.get().getBreakStartTime() != null && 
               attendance.get().getBreakEndTime() == null;
    }

    private double calculateHoursWorked(LocalTime checkIn, LocalTime checkOut, Double breakDuration) {
        double totalHours = Duration.between(checkIn, checkOut).toMinutes() / 60.0;
        double breakHours = breakDuration != null ? breakDuration : 0.0;
        return Math.max(0, totalHours - breakHours);
    }

    public Attendance saveAttendance(Attendance attendance) {
        validateAttendance(attendance);
        return attendanceRepository.save(attendance);
    }

    private void validateAttendance(Attendance attendance) {
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            if (attendance.getCheckInTime().isAfter(attendance.getCheckOutTime())) {
                throw new RuntimeException("Check-in time cannot be after check-out time");
            }
        }

        if (attendance.getBreakStartTime() != null && attendance.getBreakEndTime() != null) {
            if (attendance.getBreakStartTime().isAfter(attendance.getBreakEndTime())) {
                throw new RuntimeException("Break start time cannot be after break end time");
            }
        }

        if (attendance.getHoursWorked() != null && attendance.getHoursWorked() < 0) {
            throw new RuntimeException("Hours worked cannot be negative");
        }

        if (attendance.getOvertimeHours() != null && attendance.getOvertimeHours() < 0) {
            throw new RuntimeException("Overtime hours cannot be negative");
        }
    }
}