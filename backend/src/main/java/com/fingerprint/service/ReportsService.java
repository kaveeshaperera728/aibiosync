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

    // Existing monthly report — kept for backwards compatibility
    public List<DailyReportDto> generateMonthlyReport(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return generateFlexibleReport(ym.atDay(1), ym.atEndOfMonth(), null, null, null);
    }

    /**
     * Flexible report: filter by date range, employee, department, device.
     * Returns one row per employee per day.
     */
    public List<DailyReportDto> generateFlexibleReport(
            LocalDate from, LocalDate to,
            Long employeeId, String department, Long deviceId) {

        LocalDateTime start = from != null ? from.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime end   = to   != null ? to.atTime(23, 59, 59) : LocalDate.now().atTime(23, 59, 59);

        List<AttendanceLog> logs = attendanceLogRepository.findByPunchTimeBetween(start, end);

        // Apply filters
        logs = logs.stream()
                .filter(log -> log.getEmployee() != null)
                .filter(log -> employeeId == null || log.getEmployee().getId().equals(employeeId))
                .filter(log -> department == null || department.isBlank() ||
                        department.equalsIgnoreCase(log.getEmployee().getDepartment()))
                .filter(log -> deviceId == null || (log.getDevice() != null && log.getDevice().getId().equals(deviceId)))
                .collect(Collectors.toList());

        // Group by Date → EmployeeId
        Map<LocalDate, Map<Long, List<AttendanceLog>>> grouped = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getPunchTime().toLocalDate(),
                        Collectors.groupingBy(log -> log.getEmployee().getId())
                ));

        List<DailyReportDto> reports = new ArrayList<>();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        // Iterate all dates in range
        LocalDate cursor = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate endDate = to != null ? to : LocalDate.now();

        // Get matching employees
        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> employeeId == null || e.getId().equals(employeeId))
                .filter(e -> department == null || department.isBlank() ||
                        department.equalsIgnoreCase(e.getDepartment()))
                .collect(Collectors.toList());

        while (!cursor.isAfter(endDate)) {
            final LocalDate date = cursor;
            Map<Long, List<AttendanceLog>> dayLogs = grouped.getOrDefault(date, new HashMap<>());

            for (Employee emp : employees) {
                // If filtering by device, skip employees with no logs for that device on this day
                List<AttendanceLog> empLogs = dayLogs.get(emp.getId());
                if (deviceId != null && (empLogs == null || empLogs.isEmpty())) {
                    cursor = cursor.plusDays(1);
                    continue;
                }

                DailyReportDto dto = new DailyReportDto();
                dto.setDate(date.toString());
                dto.setEmployeeId(emp.getId());
                String fName = emp.getFirstName() != null ? emp.getFirstName() : "";
                String lName = emp.getLastName() != null ? emp.getLastName() : "";
                dto.setEmployeeName((fName + " " + lName).trim());
                dto.setEmployeeNumber(emp.getEmployeeNumber());
                dto.setDepartment(emp.getDepartment());

                if (empLogs == null || empLogs.isEmpty()) {
                    dto.setStatus("Absent");
                    dto.setFirstCheckIn("---");
                    dto.setLastCheckOut("---");
                    dto.setTotalHours("0h 0m");
                    dto.setDeviceName("---");
                } else {
                    empLogs.sort(Comparator.comparing(AttendanceLog::getPunchTime));
                    LocalDateTime firstPunch = empLogs.get(0).getPunchTime();
                    LocalDateTime lastPunch  = empLogs.get(empLogs.size() - 1).getPunchTime();

                    dto.setFirstCheckIn(firstPunch.format(timeFmt));
                    // Show the device that recorded the first punch
                    dto.setDeviceName(empLogs.get(0).getDevice() != null ? empLogs.get(0).getDevice().getName() : "---");

                    if (empLogs.size() > 1) {
                        dto.setLastCheckOut(lastPunch.format(timeFmt));
                        long mins = ChronoUnit.MINUTES.between(firstPunch, lastPunch);
                        dto.setTotalHours((mins / 60) + "h " + (mins % 60) + "m");
                    } else {
                        dto.setLastCheckOut("---");
                        dto.setTotalHours("---");
                    }

                    dto.setStatus(firstPunch.toLocalTime().isAfter(LocalTime.of(9, 15)) ? "Late" : "Present");
                }
                reports.add(dto);
            }
            cursor = cursor.plusDays(1);
        }

        return reports;
    }
}
