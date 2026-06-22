package com.fingerprint.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardDto {
    private int totalDevices;
    private int onlineDevices;
    private int queuedCommands;
    private int totalPunchesToday;
    private int onTimeArrivals;
    private int lateCheckIns;
    private int failedVerifications;

    private List<DeviceHealthDto> deviceHealth;
    private List<LiveActivityDto> liveActivity;

    @Data
    public static class DeviceHealthDto {
        private String name;
        private String ip;
        private String ping;
        private String status;
    }

    @Data
    public static class LiveActivityDto {
        private String time;
        private String employeeName;
        private String employeeId;
        private String deviceName;
        private String status;
        private String label;
    }
}
