package com.fingerprint.repository;

import com.fingerprint.entity.DeviceCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {
    List<DeviceCommand> findByDeviceIdAndStatus(Long deviceId, String status);
    List<DeviceCommand> findByDeviceSerialNumberAndStatus(String serialNumber, String status);
    List<DeviceCommand> findByStatus(String status);
}
