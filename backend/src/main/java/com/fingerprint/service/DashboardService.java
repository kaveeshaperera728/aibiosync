package com.fingerprint.service;

import com.fingerprint.dto.DashboardDto;
import com.fingerprint.entity.AttendanceLog;
import com.fingerprint.entity.Device;
import com.fingerprint.repository.AttendanceLogRepository;
import com.fingerprint.repository.DeviceCommandRepository;
import com.fingerprint.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final DeviceRepository deviceRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final DeviceCommandRepository deviceCommandRepository;

    public DashboardService(DeviceRepository deviceRepository, AttendanceLogRepository attendanceLogRepository, DeviceCommandRepository deviceCommandRepository) {
        this.deviceRepository = deviceRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.deviceCommandRepository = deviceCommandRepository;
    }

    public DashboardDto getDashboardStats() {
        DashboardDto dto = new DashboardDto();

        List<Device> devices = deviceRepository.findAll();
        
        // Calculate dynamic online status (lastSyncTime within last 10 minutes)
        LocalDateTime tenMinsAgo = LocalDateTime.now().minusMinutes(10);
        long onlineCount = devices.stream().filter(d -> d.getLastSyncTime() != null && d.getLastSyncTime().isAfter(tenMinsAgo)).count();

        dto.setTotalDevices(devices.size());
        dto.setOnlineDevices((int) onlineCount);
        dto.setQueuedCommands(deviceCommandRepository.findByStatus("PENDING").size());

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        List<AttendanceLog> todayLogs = attendanceLogRepository.findByPunchTimeBetween(startOfDay, endOfDay);

        dto.setTotalPunchesToday(todayLogs.size());
        
        int onTime = 0;
        int late = 0;
        for (AttendanceLog log : todayLogs) {
            if (log.getPunchTime().toLocalTime().isBefore(LocalTime.of(9, 15))) {
                onTime++;
            } else {
                late++;
            }
        }
        dto.setOnTimeArrivals(onTime);
        dto.setLateCheckIns(late);
        dto.setFailedVerifications(0); // Assuming devices don't send failed logs

        // Map Device Health
        List<DashboardDto.DeviceHealthDto> healthList = new ArrayList<>();
        for (Device d : devices) {
            DashboardDto.DeviceHealthDto health = new DashboardDto.DeviceHealthDto();
            health.setName(d.getName());
            health.setIp(d.getIpAddress());
            health.setPing("N/A"); // Ping not natively supported without ICMP
            boolean isOnline = d.getLastSyncTime() != null && d.getLastSyncTime().isAfter(tenMinsAgo);
            health.setStatus(isOnline ? "online" : "offline");
            healthList.add(health);
        }
        dto.setDeviceHealth(healthList);

        // Map Live Activity
        List<AttendanceLog> recentLogs = attendanceLogRepository.findTop10ByOrderByPunchTimeDesc();

        List<DashboardDto.LiveActivityDto> activities = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        for (AttendanceLog log : recentLogs) {
            DashboardDto.LiveActivityDto act = new DashboardDto.LiveActivityDto();
            act.setTime(log.getPunchTime().format(timeFormatter));
            act.setEmployeeName(log.getEmployee() != null ? log.getEmployee().getFirstName() + " " + log.getEmployee().getLastName() : "Unknown");
            act.setEmployeeId(log.getEmployee() != null ? log.getEmployee().getEmployeeNumber() : "---");
            act.setDeviceName(log.getDevice() != null ? log.getDevice().getName() : "Unknown Device");
            
            boolean isLate = log.getPunchTime().toLocalTime().isAfter(LocalTime.of(9, 15));
            act.setStatus(isLate ? "late" : "online");
            act.setLabel(isLate ? "Late In" : "Verified");
            
            activities.add(act);
        }
        dto.setLiveActivity(activities);

        return dto;
    }
}
