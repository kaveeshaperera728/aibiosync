package com.fingerprint.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "biometric_templates")
@Getter
@Setter
public class BiometricTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // The backupnum (e.g. 0-9 = Fingerprint, 10 = Password, 11 = Card, 20-27 = Face)
    @Column(name = "backup_num", nullable = false)
    private Integer backupNum;

    // Base64 encoded string from the device
    @Column(name = "template_data", columnDefinition = "LONGTEXT")
    private String templateData;
}
