package com.fingerprint.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fingerprint.entity.Device;
import com.fingerprint.entity.DeviceCommand;
import com.fingerprint.repository.DeviceCommandRepository;
import com.fingerprint.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import com.fingerprint.websocket.DeviceWebSocketHandler;
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
        
        // Push to device immediately if connected
        deviceWebSocketHandler.triggerCommandDispatch(device.getSerialNumber());
    }
    
    public void queueGetUserListAllDevices() {
        List<Device> devices = deviceRepository.findAll();
        for (Device device : devices) {
            if ("online".equals(device.getStatus())) {
                queueGetUserList(device.getId());
            }
        }
    }
}
