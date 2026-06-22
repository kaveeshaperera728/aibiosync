package com.fingerprint.repository;

import com.fingerprint.entity.DeviceUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceUserMappingRepository extends JpaRepository<DeviceUserMapping, Long> {
}

