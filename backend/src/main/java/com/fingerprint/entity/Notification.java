package com.fingerprint.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "read_status")
    private Boolean readStatus = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
