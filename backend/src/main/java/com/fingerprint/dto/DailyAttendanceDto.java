package com.fingerprint.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DailyAttendanceDto {
    private Long employeeId;
    private String employeeName;
    private String employeeNumber;
    private String department;
    private LocalDate date;
    private LocalDateTime firstIn;
    private LocalDateTime lastOut;
    private Double hoursWorked;
    private String status; // PRESENT, HALF_DAY, ABSENT
}
