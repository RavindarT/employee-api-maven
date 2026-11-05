package com.example.employee;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock EmployeeRepository repository;
    @InjectMocks EmployeeService service;

    @Test
    void createTrimsTextAndSavesEmployee() {
        when(repository.save(any(Employee.class))).thenAnswer(call -> call.getArgument(0));
        var response = service.create(new EmployeeRequest(" Ada ", 5, " Boston "));
        assertThat(response.name()).isEqualTo("Ada");
        assertThat(response.city()).isEqualTo("Boston");
        assertThat(response.yearsOfExperience()).isEqualTo(5);
        verify(repository).save(any(Employee.class));
    }

    @Test
    void missingEmployeeThrowsNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(42L))
            .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void updateChangesExistingEmployee() {
        var employee = new Employee("Ada", 2, "Boston");
        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.save(employee)).thenReturn(employee);
        var response = service.update(1L, new EmployeeRequest("Grace", 7, "New York"));
        assertThat(response.name()).isEqualTo("Grace");
        assertThat(response.yearsOfExperience()).isEqualTo(7);
        verify(repository).save(employee);
    }

    @Test
    void deleteRequiresAnExistingEmployee() {
        when(repository.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(42L))
            .isInstanceOf(EmployeeNotFoundException.class);
        verify(repository, never()).delete(any());
    }
}
