package com.fingerprint.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Getter
@Setter
public class Employee extends BaseEntity {

    @Column(name = "employee_number", nullable = false, unique = true, length = 50)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String designation;

    @Column(name = "card_number", length = 50)
    private String cardNumber;

    @Column(length = 20)
    private String pin;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(length = 20)
    private String status = "ACTIVE";

    @Column(name = "has_fingerprint")
    private Boolean hasFingerprint = false;

    @Column(name = "has_face")
    private Boolean hasFace = false;

    @Column(name = "has_card")
    private Boolean hasCard = false;

    @Column(name = "has_password")
    private Boolean hasPassword = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToMany
    @JoinTable(
        name = "employee_devices",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "device_id")
    )
    private Set<Device> registeredDevices = new HashSet<>();
}
