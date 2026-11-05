package com.example.employee;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {
    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public EmployeeResponse get(Long empid) {
        return EmployeeResponse.from(find(empid));
    }

    public EmployeePage list(int page, int size) {
        Page<EmployeeResponse> result = repository.findAll(
            PageRequest.of(page, size, Sort.by("empid"))).map(EmployeeResponse::from);
        return new EmployeePage(result.getContent(), result.getNumber(), result.getSize(),
            result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        return EmployeeResponse.from(repository.save(new Employee(
            request.name().strip(), request.yearsOfExperience(), request.city().strip())));
    }

    @Transactional
    public EmployeeResponse update(Long empid, EmployeeRequest request) {
        Employee employee = find(empid);
        employee.update(request.name().strip(), request.yearsOfExperience(), request.city().strip());
        return EmployeeResponse.from(repository.save(employee));
    }

    @Transactional
    public void delete(Long empid) {
        repository.delete(find(empid));
    }

    private Employee find(Long empid) {
        return repository.findById(empid).orElseThrow(() -> new EmployeeNotFoundException(empid));
    }
}
