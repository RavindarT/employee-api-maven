package com.example.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean EmployeeService service;

    @Test
    void createReturns201AndLocation() throws Exception {
        when(service.create(any())).thenReturn(new EmployeeResponse(1L, "Ada", 5, "Boston"));
        mvc.perform(post("/api/employees").contentType("application/json")
            .content("""
                {"name":"Ada","yearsOfExperience":5,"city":"Boston"}
                """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/api/employees/1"))
            .andExpect(jsonPath("$.empid").value(1));
    }

    @Test
    void invalidFieldsReturn400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/employees").contentType("application/json")
            .content("""
                {"name":" ","yearsOfExperience":-1,"city":""}
                """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.yearsOfExperience").exists())
            .andExpect(jsonPath("$.errors.city").exists());
        verifyNoInteractions(service);
    }

    @Test
    void missingEmployeeReturns404() throws Exception {
        when(service.get(99L)).thenThrow(new EmployeeNotFoundException(99L));
        mvc.perform(get("/api/employees/99")).andExpect(status().isNotFound());
    }

    @Test
    void invalidPaginationReturns400() throws Exception {
        mvc.perform(get("/api/employees?size=101")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/employees").contentType("application/json").content("{"))
            .andExpect(status().isBadRequest());
    }
}
