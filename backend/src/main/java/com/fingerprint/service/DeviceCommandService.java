package com.fingerprint.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fingerprint.entity.Device;
import com.fingerprint.entity.DeviceCommand;
import com.fingerprint.repository.DeviceCommandRepository;
import com.fingerprint.repository.DeviceRepository;
import com.fingerprint.websocket.DeviceWebSocketHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DeviceCommandService {

    private final DeviceCommandRepository deviceCommandRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceWebSocketHandler deviceWebSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeviceCommandService(DeviceCommandRepository deviceCommandRepository,
                                DeviceRepository deviceRepository,
                                DeviceWebSocketHandler deviceWebSocketHandler) {
        this.deviceCommandRepository = deviceCommandRepository;
        this.deviceRepository = deviceRepository;
        this.deviceWebSocketHandler = deviceWebSocketHandler;
    }

    public void queueGetUserList(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("cmd", "getuserlist");
        payload.put("stn", true);

        DeviceCommand cmd = new DeviceCommand();
        cmd.setDevice(device);
        cmd.setCommandType("getuserlist");
        cmd.setCommandPayload(payload.toString());
        cmd.setStatus("PENDING");

        deviceCommandRepository.save(cmd);
        deviceWebSocketHandler.triggerCommandDispatch(device.getSerialNumber());
    }

    public void queueGetUserListAllDevices() {
        List<Device> devices = deviceRepository.findAll();
        for (Device device : devices) {
            boolean isOnline = device.getLastSyncTime() != null &&
                    device.getLastSyncTime().isAfter(LocalDateTime.now().minusMinutes(10));
            if (isOnline) {
                queueGetUserList(device.getId());
            }
        }
    }

    public void queueSyncTimeCommand(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("cmd", "settime");
        payload.put("cloudtime", currentTime);

        DeviceCommand cmd = new DeviceCommand();
        cmd.setDevice(device);
        cmd.setCommandType("settime");
        cmd.setCommandPayload(payload.toString());
        cmd.setStatus("PENDING");

        deviceCommandRepository.save(cmd);
        deviceWebSocketHandler.triggerCommandDispatch(device.getSerialNumber());
    }
}
