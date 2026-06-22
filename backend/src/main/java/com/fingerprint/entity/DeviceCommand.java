package com.fingerprint.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_commands")
@Getter
@Setter
public class DeviceCommand extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "command_payload", columnDefinition = "TEXT", nullable = false)
    private String commandPayload;

    // Status: PENDING, SENT, SUCCESS, FAILED
    @Column(nullable = false, length = 20)
    private String status = "PENDING";
    
    @Column(name = "command_type", length = 50)
    private String commandType; // e.g. "getuserlist", "setuserinfo", "opendoor"
}
