package com.example.employee;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeIntegrationTest {
    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void fullCrudAgainstPostgresWithFlyway() throws Exception {
        String created = mvc.perform(post("/api/employees").contentType("application/json")
            .content("""
                {"name":"Ada","yearsOfExperience":5,"city":"Boston"}
                """))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(created).get("empid").asLong();
        String path = "/api/employees/" + id;

        mvc.perform(get(path)).andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Ada"));
        mvc.perform(get("/api/employees")).andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(put(path).contentType("application/json")
            .content("""
                {"name":"Ada","yearsOfExperience":6,"city":"Seattle"}
                """))
            .andExpect(status().isOk()).andExpect(jsonPath("$.city").value("Seattle"));
        mvc.perform(delete(path)).andExpect(status().isNoContent());
        mvc.perform(get(path)).andExpect(status().isNotFound());
        mvc.perform(delete(path)).andExpect(status().isNotFound());
    }
}
