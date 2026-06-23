package com.fingerprint.dto;

import lombok.Data;

@Data
public class DailyReportDto {
    private String date;
    private Long employeeId;
    private String employeeName;
    private String employeeNumber;
    private String department;
    private String deviceName;
    private String firstCheckIn;
    private String lastCheckOut;
    private String status; // Present, Absent, Late
    private String totalHours;
}
