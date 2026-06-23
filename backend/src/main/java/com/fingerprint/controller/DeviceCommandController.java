package com.fingerprint.controller;

import com.fingerprint.service.DeviceCommandService;
import com.fingerprint.websocket.DeviceWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commands")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeviceCommandController {

    private final DeviceCommandService deviceCommandService;
    private final DeviceWebSocketHandler deviceWebSocketHandler;

    public DeviceCommandController(DeviceCommandService deviceCommandService,
                                   DeviceWebSocketHandler deviceWebSocketHandler) {
        this.deviceCommandService = deviceCommandService;
        this.deviceWebSocketHandler = deviceWebSocketHandler;
    }

    @PostMapping("/sync-users")
    public ResponseEntity<?> syncUsers() {
        deviceCommandService.queueGetUserListAllDevices();
        return ResponseEntity.ok().body("{\"message\": \"User sync command queued for all active devices.\"}");
    }

    @PostMapping("/{deviceId}/sync-time")
    public ResponseEntity<?> syncTime(@PathVariable Long deviceId) {
        try {
            deviceCommandService.queueSyncTimeCommand(deviceId);
            return ResponseEntity.ok(Map.of("message", "Time sync sent successfully to device."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/logs")
    public ResponseEntity<?> getDeviceLogs() {
        return ResponseEntity.ok(deviceWebSocketHandler.getRecentPayloads());
    }
}
