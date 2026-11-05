package com.example.employee;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse result = service.create(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{empid}").buildAndExpand(result.empid()).toUri()).body(result);
    }

    @GetMapping
    public EmployeePage list(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(page, size);
    }

    @GetMapping("/{empid}")
    public EmployeeResponse get(@PathVariable @Positive Long empid) {
        return service.get(empid);
    }

    @PutMapping("/{empid}")
    public EmployeeResponse update(@PathVariable @Positive Long empid,
                                   @Valid @RequestBody EmployeeRequest request) {
        return service.update(empid, request);
    }

    @DeleteMapping("/{empid}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long empid) {
        service.delete(empid);
        return ResponseEntity.noContent().build();
    }
}
