package com.fingerprint.service;

import com.fingerprint.dto.DeviceDto;
import com.fingerprint.dto.EmployeeDto;
import com.fingerprint.entity.Device;
import com.fingerprint.entity.Employee;
import com.fingerprint.entity.BiometricTemplate;
import com.fingerprint.entity.DeviceCommand;
import com.fingerprint.repository.DeviceRepository;
import com.fingerprint.repository.EmployeeRepository;
import com.fingerprint.repository.BiometricTemplateRepository;
import com.fingerprint.repository.DeviceCommandRepository;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.stream.Collectors;

import com.fingerprint.websocket.DeviceWebSocketHandler;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final EmployeeRepository employeeRepository;
    private final BiometricTemplateRepository biometricTemplateRepository;
    private final DeviceCommandRepository deviceCommandRepository;
    private final DeviceWebSocketHandler deviceWebSocketHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DeviceService(DeviceRepository deviceRepository,
                         EmployeeRepository employeeRepository,
                         BiometricTemplateRepository biometricTemplateRepository,
                         DeviceCommandRepository deviceCommandRepository,
                         DeviceWebSocketHandler deviceWebSocketHandler) {
        this.deviceRepository = deviceRepository;
        this.employeeRepository = employeeRepository;
        this.biometricTemplateRepository = biometricTemplateRepository;
        this.deviceCommandRepository = deviceCommandRepository;
        this.deviceWebSocketHandler = deviceWebSocketHandler;
    }

    public List<DeviceDto> getAllDevices() {
        return deviceRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public DeviceDto getDeviceById(Long id) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Device not found"));
        return convertToDto(device);
    }

    public DeviceDto createDevice(DeviceDto deviceDto) {
        Device device = new Device();
        updateEntityFromDto(device, deviceDto);
        return convertToDto(deviceRepository.save(device));
    }

    public DeviceDto updateDevice(Long id, DeviceDto deviceDto) {
        Device device = deviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Device not found"));
        updateEntityFromDto(device, deviceDto);
        return convertToDto(deviceRepository.save(device));
    }

    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }

    private void updateEntityFromDto(Device device, DeviceDto dto) {
        device.setName(dto.getName());
        device.setIpAddress(dto.getIpAddress());
        device.setPort(dto.getPort() != null ? dto.getPort() : 8081);
        device.setSerialNumber(dto.getSerialNumber());
        device.setLocation(dto.getLocation());
        device.setDeviceType(dto.getDeviceType());
        if (dto.getStatus() != null) {
            device.setStatus(dto.getStatus());
        }
    }

    private DeviceDto convertToDto(Device device) {
        DeviceDto dto = new DeviceDto();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setIpAddress(device.getIpAddress());
        dto.setPort(device.getPort());
        dto.setSerialNumber(device.getSerialNumber());
        dto.setLocation(device.getLocation());
        dto.setDeviceType(device.getDeviceType());
        dto.setFirmwareVersion(device.getFirmwareVersion());
        
        // Dynamic status check
        if (device.getLastSyncTime() != null && device.getLastSyncTime().isAfter(java.time.LocalDateTime.now().minusMinutes(10))) {
            dto.setStatus("online");
        } else {
            dto.setStatus("offline");
        }
        
        dto.setMacAddress(device.getMacAddress());
        dto.setUserCount(device.getUserCount());
        dto.setFingerprintCount(device.getFingerprintCount());
        dto.setFaceCount(device.getFaceCount());
        dto.setLogCount(device.getLogCount());
        return dto;
    }

    public List<EmployeeDto> getMissingUsersForDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        return employeeRepository.findAll().stream()
                .filter(emp -> !emp.getRegisteredDevices().contains(device))
                .map(emp -> {
                    EmployeeDto dto = new EmployeeDto();
                    dto.setId(emp.getId());
                    dto.setEmployeeNumber(emp.getEmployeeNumber());
                    dto.setFirstName(emp.getFirstName());
                    dto.setLastName(emp.getLastName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void provisionUsersToDevice(Long deviceId, List<Long> employeeIds) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        for (Long empId : employeeIds) {
            Employee emp = employeeRepository.findById(empId).orElse(null);
            if (emp != null) {
                List<BiometricTemplate> templates = biometricTemplateRepository.findByEmployee(emp);
                
                // 1. ALWAYS push backupnum=50 first to create the user profile, name, and face photo (if exists)
                BiometricTemplate faceTemplate = templates.stream().filter(t -> t.getBackupNum() == 50).findFirst().orElse(null);
                queueSetUserInfoCommand(device, emp, 50, faceTemplate != null ? faceTemplate.getTemplateData() : "");

                // 2. Push any other templates (e.g., fingerprints 0-9)
                for (BiometricTemplate template : templates) {
                    if (template.getBackupNum() != 50) {
                        queueSetUserInfoCommand(device, emp, template.getBackupNum(), template.getTemplateData());
                    }
                }

                // Optimistically link them
                emp.getRegisteredDevices().add(device);
                employeeRepository.save(emp);
            }
        }
        // Immediately push all queued commands to the device via WebSocket
        deviceWebSocketHandler.triggerCommandDispatch(device.getSerialNumber());
    }

    private void queueSetUserInfoCommand(Device device, Employee emp, int backupNum, String record) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("cmd", "setuserinfo");
        payload.put("enrollid", Integer.parseInt(emp.getEmployeeNumber()));
        payload.put("name", emp.getFirstName());
        payload.put("backupnum", backupNum);
        payload.put("admin", 0);
        
        // If this is the main user record (backupnum 50)
        if (backupNum == 50) {
            payload.put("faceflag", (emp.getHasFace() != null && emp.getHasFace()) ? 1 : 0);
            payload.put("fpflag", (emp.getHasFingerprint() != null && emp.getHasFingerprint()) ? 1 : 0);
            payload.put("enable", 1);
            
            // If the record is a JPEG photo, we MUST include a photourl so the device knows how to parse it
            if (record != null && record.startsWith("/9j/")) {
                if (emp.getPhotoUrl() != null && !emp.getPhotoUrl().isEmpty()) {
                    payload.put("photourl", emp.getPhotoUrl());
                } else {
                    String paddedId = String.format("%08d", Integer.parseInt(emp.getEmployeeNumber()));
                    payload.put("photourl", "/photos/LF" + paddedId + ".jpg");
                }
            }
        }
        
        if (record != null && !record.isEmpty()) {
            payload.put("record", record);
        }

        DeviceCommand cmd = new DeviceCommand();
        cmd.setDevice(device);
        cmd.setCommandType("setuserinfo");
        cmd.setCommandPayload(payload.toString());
        cmd.setStatus("PENDING");
        deviceCommandRepository.save(cmd);
    }
}
