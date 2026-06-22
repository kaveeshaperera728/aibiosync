package com.fingerprint.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeNumber;
    private Long deviceId;
    private String deviceName;
    private LocalDateTime punchTime;
    private Integer verificationType;
    private Integer direction;
}
