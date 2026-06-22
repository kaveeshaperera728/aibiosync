package com.fingerprint.repository;

import com.fingerprint.entity.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
    List<AttendanceLog> findByPunchTimeBetween(LocalDateTime start, LocalDateTime end);
    List<AttendanceLog> findTop10ByOrderByPunchTimeDesc();
    List<AttendanceLog> findByEmployeeIdAndPunchTimeBetweenOrderByPunchTimeAsc(Long employeeId, LocalDateTime start, LocalDateTime end);
}

