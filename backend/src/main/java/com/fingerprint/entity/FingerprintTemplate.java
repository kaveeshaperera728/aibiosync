package com.fingerprint.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fingerprint_templates")
@Getter
@Setter
public class FingerprintTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "finger_index", nullable = false)
    private Integer fingerIndex;

    @Lob
    @Column(name = "template_data", nullable = false, columnDefinition = "TEXT")
    private String templateData;
}
