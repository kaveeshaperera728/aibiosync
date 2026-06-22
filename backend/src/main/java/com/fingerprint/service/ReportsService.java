package com.fingerprint.service;

import com.fingerprint.dto.DailyReportDto;
import com.fingerprint.entity.AttendanceLog;
import com.fingerprint.entity.Employee;
import com.fingerprint.repository.AttendanceLogRepository;
import com.fingerprint.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final EmployeeRepository employeeRepository;

    public ReportsService(AttendanceLogRepository attendanceLogRepository, EmployeeRepository employeeRepository) {
        this.attendanceLogRepository = attendanceLogRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<DailyReportDto> generateMonthlyReport(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime startOfMonth = ym.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = ym.atEndOfMonth().atTime(23, 59, 59);

        List<Employee> allEmployees = employeeRepository.findAll();
        List<AttendanceLog> logs = attendanceLogRepository.findByPunchTimeBetween(startOfMonth, endOfMonth);

        // Group by Date, then by Employee
        Map<LocalDate, Map<Long, List<AttendanceLog>>> groupedLogs = logs.stream()
                .filter(log -> log.getEmployee() != null)
                .collect(Collectors.groupingBy(
                        log -> log.getPunchTime().toLocalDate(),
                        Collectors.groupingBy(log -> log.getEmployee().getId())
                ));

        List<DailyReportDto> reports = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            Map<Long, List<AttendanceLog>> dayLogs = groupedLogs.getOrDefault(date, new HashMap<>());

            for (Employee emp : allEmployees) {
                DailyReportDto dto = new DailyReportDto();
                dto.setDate(date.toString());
                dto.setEmployeeId(emp.getId());
                dto.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());
                dto.setEmployeeNumber(emp.getEmployeeNumber());

                List<AttendanceLog> empLogs = dayLogs.get(emp.getId());

                if (empLogs == null || empLogs.isEmpty()) {
                    dto.setStatus("Absent");
                    dto.setFirstCheckIn("---");
                    dto.setLastCheckOut("---");
                    dto.setTotalHours("0h 0m");
                } else {
                    empLogs.sort(Comparator.comparing(AttendanceLog::getPunchTime));
                    LocalDateTime firstPunch = empLogs.get(0).getPunchTime();
                    LocalDateTime lastPunch = empLogs.get(empLogs.size() - 1).getPunchTime();

                    dto.setFirstCheckIn(firstPunch.format(timeFormatter));
                    
                    if (empLogs.size() > 1) {
                        dto.setLastCheckOut(lastPunch.format(timeFormatter));
                        long minutes = ChronoUnit.MINUTES.between(firstPunch, lastPunch);
                        long hours = minutes / 60;
                        long remMins = minutes % 60;
                        dto.setTotalHours(hours + "h " + remMins + "m");
                    } else {
                        dto.setLastCheckOut("---");
                        dto.setTotalHours("---");
                    }

                    // Check if late (after 09:15)
                    if (firstPunch.toLocalTime().isAfter(LocalTime.of(9, 15))) {
                        dto.setStatus("Late");
                    } else {
                        dto.setStatus("Present");
                    }
                }
                reports.add(dto);
            }
        }

        return reports;
    }
}
