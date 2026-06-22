package com.fingerprint.dto;

import lombok.Data;

@Data
public class EmployeeDto {
    private Long id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String department;
    private String designation;
    private String cardNumber;
    private String pin;
    private String photoUrl;
    private String status;
    private boolean hasFingerprint;
    private boolean hasFace;
    private boolean hasPassword;
    private boolean hasCard;
}
