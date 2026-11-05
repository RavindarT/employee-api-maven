package com.example.employee;

public record EmployeeResponse(Long empid, String name, Integer yearsOfExperience, String city) {
    static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(employee.getEmpid(), employee.getName(),
            employee.getYearsOfExperience(), employee.getCity());
    }
}
