package com.fingerprint.controller;

import com.fingerprint.service.DeviceCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commands")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeviceCommandController {

    private final DeviceCommandService deviceCommandService;

    private final com.fingerprint.websocket.DeviceWebSocketHandler deviceWebSocketHandler;

    public DeviceCommandController(DeviceCommandService deviceCommandService, com.fingerprint.websocket.DeviceWebSocketHandler deviceWebSocketHandler) {
        this.deviceCommandService = deviceCommandService;
        this.deviceWebSocketHandler = deviceWebSocketHandler;
    }

    @PostMapping("/sync-users")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> syncUsers() {
        deviceCommandService.queueGetUserListAllDevices();
        return ResponseEntity.ok().body("{\"message\": \"User sync command queued for all active devices.\"}");
    }
    
    @GetMapping("/logs")
    public ResponseEntity<?> getDeviceLogs() {
        return ResponseEntity.ok(deviceWebSocketHandler.getRecentPayloads());
    }
}
