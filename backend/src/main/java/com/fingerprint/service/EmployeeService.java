package com.fingerprint.service;

import com.fingerprint.dto.EmployeeDto;
import com.fingerprint.entity.Employee;
import com.fingerprint.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final com.fingerprint.repository.DeviceCommandRepository deviceCommandRepository;
    private final com.fingerprint.websocket.DeviceWebSocketHandler deviceWebSocketHandler;

    public EmployeeService(EmployeeRepository employeeRepository, 
                           com.fingerprint.repository.DeviceCommandRepository deviceCommandRepository,
                           com.fingerprint.websocket.DeviceWebSocketHandler deviceWebSocketHandler) {
        this.employeeRepository = employeeRepository;
        this.deviceCommandRepository = deviceCommandRepository;
        this.deviceWebSocketHandler = deviceWebSocketHandler;
    }

    public List<EmployeeDto> getAllEmployees() {
        return employeeRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        return convertToDto(employee);
    }

    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        Employee employee = new Employee();
        updateEntityFromDto(employee, employeeDto);
        return convertToDto(employeeRepository.save(employee));
    }

    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        updateEntityFromDto(employee, employeeDto);
        return convertToDto(employeeRepository.save(employee));
    }

    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Push deluser command to all registered devices before deleting from database
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        for (com.fingerprint.entity.Device device : employee.getRegisteredDevices()) {
            try {
                com.fasterxml.jackson.databind.node.ObjectNode payload = mapper.createObjectNode();
                payload.put("cmd", "deluser");
                payload.put("enrollid", Integer.parseInt(employee.getEmployeeNumber()));

                com.fingerprint.entity.DeviceCommand cmd = new com.fingerprint.entity.DeviceCommand();
                cmd.setDevice(device);
                cmd.setCommandType("deluser");
                cmd.setCommandPayload(payload.toString());
                cmd.setStatus("PENDING");
                deviceCommandRepository.save(cmd);

                deviceWebSocketHandler.triggerCommandDispatch(device.getSerialNumber());
            } catch (Exception e) {
                // Log and continue if one device fails
                System.err.println("Failed to queue deluser for device " + device.getSerialNumber());
            }
        }
        
        employeeRepository.deleteById(id);
    }

    private void updateEntityFromDto(Employee employee, EmployeeDto dto) {
        employee.setEmployeeNumber(dto.getEmployeeNumber());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setDepartment(dto.getDepartment());
        employee.setDesignation(dto.getDesignation());
        employee.setCardNumber(dto.getCardNumber());
        employee.setPin(dto.getPin());
        employee.setPhotoUrl(dto.getPhotoUrl());
        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }
    }

    private EmployeeDto convertToDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setEmployeeNumber(employee.getEmployeeNumber());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setDepartment(employee.getDepartment());
        dto.setDesignation(employee.getDesignation());
        dto.setCardNumber(employee.getCardNumber());
        dto.setPin(employee.getPin());
        dto.setPhotoUrl(employee.getPhotoUrl());
        dto.setStatus(employee.getStatus());
        dto.setHasFingerprint(Boolean.TRUE.equals(employee.getHasFingerprint()));
        dto.setHasFace(Boolean.TRUE.equals(employee.getHasFace()));
        dto.setHasPassword(Boolean.TRUE.equals(employee.getHasPassword()));
        dto.setHasCard(Boolean.TRUE.equals(employee.getHasCard()));
        return dto;
    }
}
