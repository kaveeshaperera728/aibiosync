package com.fingerprint.service;

import com.fingerprint.dto.AttendanceDto;
import com.fingerprint.entity.AttendanceLog;
import com.fingerprint.repository.AttendanceLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceLogRepository attendanceLogRepository;

    public AttendanceService(AttendanceLogRepository attendanceLogRepository) {
        this.attendanceLogRepository = attendanceLogRepository;
    }

    public List<AttendanceDto> getAllAttendanceLogs() {
        return attendanceLogRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<AttendanceDto> getAttendanceByEmployee(Long employeeId) {
        return attendanceLogRepository.findAll().stream()
                .filter(log -> log.getEmployee().getId().equals(employeeId))
                .map(this::convertToDto)
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
