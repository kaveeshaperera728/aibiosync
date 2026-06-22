package com.fingerprint.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sync_jobs")
@Getter
@Setter
public class SyncJob extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "success_count")
    private Integer successCount = 0;

    @Column(name = "failed_count")
    private Integer failedCount = 0;

    @Column(columnDefinition = "TEXT")
    private String details;
}
