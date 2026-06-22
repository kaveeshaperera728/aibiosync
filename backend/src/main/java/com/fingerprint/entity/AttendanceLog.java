package com.fingerprint.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_logs")
@Getter
@Setter
public class AttendanceLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    @Column(name = "punch_time", nullable = false)
    private LocalDateTime punchTime;

    @Column(name = "verification_type")
    private Integer verificationType;

    @Column
    private Integer direction;
}
