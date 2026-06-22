package com.fingerprint.repository;

import com.fingerprint.entity.FingerprintTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FingerprintTemplateRepository extends JpaRepository<FingerprintTemplate, Long> {
}

