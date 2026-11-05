package com.example.employee;

import java.util.List;

public record EmployeePage(List<EmployeeResponse> content, int page, int size,
                           long totalElements, int totalPages) {}
