package com.fingerprint.controller;

import com.fingerprint.dto.DailyReportDto;
import com.fingerprint.service.ReportsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('SUPER_ADMIN') or hasRole('OPERATOR')")
    public ResponseEntity<List<DailyReportDto>> getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(reportsService.generateMonthlyReport(year, month));
    }
}
