package com.fingerprint.dto;

import lombok.Data;

@Data
public class DeviceDto {
    private Long id;
    private String name;
    private String ipAddress;
    private Integer port;
    private String serialNumber;
    private String location;
    private String deviceType;
    private String firmwareVersion;
    private String status;
    private String macAddress;
    private Integer userCount;
    private Integer fingerprintCount;
    private Integer faceCount;
    private Integer logCount;
}
