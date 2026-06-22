package com.fingerprint.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_logs")
@Getter
@Setter
public class EventLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String description;
}
