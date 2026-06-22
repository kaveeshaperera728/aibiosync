package com.fingerprint.controller;

import com.fingerprint.dto.AttendanceDto;
import com.fingerprint.service.AttendanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN') or hasRole('OPERATOR') or hasRole('VIEWER')")
    public List<AttendanceDto> getAllAttendance() {
        return attendanceService.getAllAttendanceLogs();
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN') or hasRole('OPERATOR') or hasRole('VIEWER')")
    public List<AttendanceDto> getAttendanceByEmployee(@PathVariable Long employeeId) {
        return attendanceService.getAttendanceByEmployee(employeeId);
    }
}
