package com.fingerprint.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "device_user_mapping", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"device_id", "employee_id"})
})
@Getter
@Setter
public class DeviceUserMapping extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "sync_status", length = 20)
    private String syncStatus = "PENDING";
}
