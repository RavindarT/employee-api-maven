package com.example.employee;

import jakarta.validation.constraints.*;

public record EmployeeRequest(
    @NotBlank @Size(max = 100) String name,
    @NotNull @Min(0) @Max(80) Integer yearsOfExperience,
    @NotBlank @Size(max = 100) String city
) {}
