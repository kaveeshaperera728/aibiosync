package com.fingerprint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FingerprintApplication {

    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public FingerprintApplication(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static void main(String[] args) {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Colombo"));
        SpringApplication.run(FingerprintApplication.class, args);
    }

    @jakarta.annotation.PostConstruct
    public void migrateDatabase() {
        try {
            jdbcTemplate.execute("ALTER TABLE biometric_templates MODIFY COLUMN template_data LONGTEXT");
            System.out.println("✅ Successfully migrated template_data column to LONGTEXT.");
        } catch (Exception e) {
            System.err.println("⚠️ Could not run migration: " + e.getMessage());
        }
    }
}
