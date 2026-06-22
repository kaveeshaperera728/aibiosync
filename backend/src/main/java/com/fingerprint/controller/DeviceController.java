package com.fingerprint.controller;

import com.fingerprint.dto.DeviceDto;
import com.fingerprint.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN') or hasRole('OPERATOR')")
    public List<DeviceDto> getAllDevices() {
        return deviceService.getAllDevices();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<DeviceDto> createDevice(@RequestBody DeviceDto deviceDto) {
        return ResponseEntity.ok(deviceService.createDevice(deviceDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<DeviceDto> updateDevice(@PathVariable Long id, @RequestBody DeviceDto deviceDto) {
        return ResponseEntity.ok(deviceService.updateDevice(id, deviceDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/missing-users")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<List<com.fingerprint.dto.EmployeeDto>> getMissingUsers(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getMissingUsersForDevice(id));
    }

    @PostMapping("/{id}/provision-users")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> provisionUsers(@PathVariable Long id, @RequestBody List<Long> employeeIds) {
        deviceService.provisionUsersToDevice(id, employeeIds);
        return ResponseEntity.ok().build();
    }
}
