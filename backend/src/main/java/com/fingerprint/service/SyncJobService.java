package com.fingerprint.service;

import com.fingerprint.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SyncJobService {

    private static final Logger logger = LoggerFactory.getLogger(SyncJobService.class);
    private final DeviceRepository deviceRepository;

    public SyncJobService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    // Run every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void monitorDeviceStatus() {
        logger.info("Running scheduled device status monitor at {}", LocalDateTime.now());
        
        // Example: Mark devices offline if no heartbeat/sync received recently
        // For now, just logging the check
        deviceRepository.findAll().forEach(device -> {
            if ("online".equals(device.getStatus())) {
                logger.debug("Device {} is online", device.getSerialNumber());
                // In a real implementation, we would check the last active timestamp
            }
        });
    }
}
