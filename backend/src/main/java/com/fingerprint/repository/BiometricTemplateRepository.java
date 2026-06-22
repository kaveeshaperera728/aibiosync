package com.fingerprint.repository;

import com.fingerprint.entity.BiometricTemplate;
import com.fingerprint.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BiometricTemplateRepository extends JpaRepository<BiometricTemplate, Long> {
    List<BiometricTemplate> findByEmployee(Employee employee);
    Optional<BiometricTemplate> findByEmployeeAndBackupNum(Employee employee, Integer backupNum);
}
