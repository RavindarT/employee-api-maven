package com.example.employee;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Long empid) {
        super("Employee " + empid + " was not found");
    }
}
