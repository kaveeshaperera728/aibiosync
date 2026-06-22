package com.fingerprint.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
@Getter
@Setter
public class Device extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(columnDefinition = "int default 8081")
    private Integer port;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Column(length = 200)
    private String location;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @Column(length = 20)
    private String status = "OFFLINE";

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

    @Column(name = "mac_address", length = 50)
    private String macAddress;

    @Column(name = "user_count")
    private Integer userCount = 0;

    @Column(name = "fingerprint_count")
    private Integer fingerprintCount = 0;

    @Column(name = "face_count")
    private Integer faceCount = 0;

    @Column(name = "log_count")
    private Integer logCount = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
