package com.fingerprint.service;

import com.fingerprint.dto.AttendanceDto;
import com.fingerprint.dto.DailyAttendanceDto;
import com.fingerprint.entity.AttendanceLog;
import com.fingerprint.repository.AttendanceLogRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceLogRepository attendanceLogRepository;

    public AttendanceService(AttendanceLogRepository attendanceLogRepository) {
        this.attendanceLogRepository = attendanceLogRepository;
    }

    public List<AttendanceDto> getAllAttendanceLogs() {
        return attendanceLogRepository.findAll().stream()
                .sorted(Comparator.comparing(AttendanceLog::getPunchTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AttendanceDto> getAttendanceByEmployee(Long employeeId) {
        return attendanceLogRepository.findAll().stream()
                .filter(log -> log.getEmployee() != null && log.getEmployee().getId().equals(employeeId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<DailyAttendanceDto> getDailyAttendance(LocalDate date) {
        List<AttendanceLog> logs = attendanceLogRepository.findAll().stream()
                .filter(log -> log.getPunchTime() != null && log.getEmployee() != null)
                .filter(log -> date == null || log.getPunchTime().toLocalDate().equals(date))
                .collect(Collectors.toList());

        // Group by employeeId + date
        Map<String, List<AttendanceLog>> grouped = logs.stream()
                .collect(Collectors.groupingBy(log ->
                        log.getEmployee().getId() + "_" + log.getPunchTime().toLocalDate()));

        return grouped.entrySet().stream().map(entry -> {
            List<AttendanceLog> empLogs = entry.getValue();
            AttendanceLog firstLog = empLogs.stream()
                    .min(Comparator.comparing(AttendanceLog::getPunchTime)).orElse(null);
            AttendanceLog lastLog = empLogs.stream()
                    .max(Comparator.comparing(AttendanceLog::getPunchTime)).orElse(null);

            DailyAttendanceDto dto = new DailyAttendanceDto();
            if (firstLog != null && firstLog.getEmployee() != null) {
                dto.setEmployeeId(firstLog.getEmployee().getId());
                String fName = firstLog.getEmployee().getFirstName() != null ? firstLog.getEmployee().getFirstName() : "";
                String lName = firstLog.getEmployee().getLastName() != null ? firstLog.getEmployee().getLastName() : "";
                String fullName = (fName + " " + lName).trim();
                dto.setEmployeeName(fullName.isEmpty() ? "Unknown" : fullName);
                dto.setEmployeeNumber(firstLog.getEmployee().getEmployeeNumber());
                dto.setDepartment(firstLog.getEmployee().getDepartment());
            }

            LocalDateTime firstIn = firstLog != null ? firstLog.getPunchTime() : null;
            LocalDateTime lastOut = lastLog != null ? lastLog.getPunchTime() : null;
            dto.setDate(firstIn != null ? firstIn.toLocalDate() : null);
            dto.setFirstIn(firstIn);
            // Only set last out if it's a different punch than first in
            dto.setLastOut(firstIn != null && lastOut != null && !firstIn.equals(lastOut) ? lastOut : null);

            // Calculate hours worked
            if (firstIn != null && lastOut != null && !firstIn.equals(lastOut)) {
                double hours = Duration.between(firstIn, lastOut).toMinutes() / 60.0;
                dto.setHoursWorked(Math.round(hours * 100.0) / 100.0);
                dto.setStatus(hours >= 4 ? "PRESENT" : "HALF_DAY");
            } else {
                dto.setHoursWorked(0.0);
                dto.setStatus("PRESENT");
            }

            return dto;
        })
        .sorted(Comparator.comparing((DailyAttendanceDto d) -> d.getDate() != null ? d.getDate() : LocalDate.MIN)
                .reversed()
                .thenComparing(d -> d.getFirstIn() != null ? d.getFirstIn() : LocalDateTime.MIN))
        .collect(Collectors.toList());
    }

    private AttendanceDto convertToDto(AttendanceLog log) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(log.getId());

        if (log.getEmployee() != null) {
            dto.setEmployeeId(log.getEmployee().getId());
            String fName = log.getEmployee().getFirstName() != null ? log.getEmployee().getFirstName() : "";
            String lName = log.getEmployee().getLastName() != null ? log.getEmployee().getLastName() : "";
            String fullName = (fName + " " + lName).trim();
            dto.setEmployeeName(fullName.isEmpty() ? "Unknown User" : fullName);
            dto.setEmployeeNumber(log.getEmployee().getEmployeeNumber());
        }

        if (log.getDevice() != null) {
            dto.setDeviceId(log.getDevice().getId());
            dto.setDeviceName(log.getDevice().getName());
        }

        dto.setPunchTime(log.getPunchTime());
        dto.setVerificationType(log.getVerificationType());
        dto.setDirection(log.getDirection());
        return dto;
    }
}
