package com.reliaquest.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.CachedEmployeeService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CachedEmployeeService cachedEmployeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee createEmployee(String id, String name, Integer salary) {
        return Employee.builder()
                .id(id)
                .employeeName(name)
                .employeeSalary(salary)
                .employeeAge(30)
                .employeeTitle("Developer")
                .employeeEmail(name.toLowerCase().replace(" ", ".") + "@company.com")
                .build();
    }

    @Test
    void getAllEmployees_Success() throws Exception {
        List<Employee> employees = Arrays.asList(
                createEmployee("1", "Soumadipta Roy", 50000), createEmployee("2", "Somantika Sarkar", 60000));
        when(cachedEmployeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].employee_name").value("Soumadipta Roy"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].employee_name").value("Somantika Sarkar"));
    }

    @Test
    void getAllEmployees_EmptyList() throws Exception {
        when(cachedEmployeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        var result = mockMvc.perform(get("/api/v1/employee"));
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllEmployees_ServiceException() throws Exception {
        when(cachedEmployeeService.getAllEmployees()).thenThrow(new EmployeeServiceException("Service error"));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Service error"))
                .andExpect(jsonPath("$.message").value("Service error"));
    }

    @Test
    void getEmployeesByNameSearch_Success() throws Exception {
        List<Employee> employees = Arrays.asList(createEmployee("1", "Soumadipta Roy", 50000));
        when(cachedEmployeeService.getEmployeesByNameSearch("Soumadipta")).thenReturn(employees);

        mockMvc.perform(get("/api/v1/employee/search/Soumadipta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employee_name").value("Soumadipta Roy"));
    }

    @Test
    void getEmployeeById_Success() throws Exception {
        Employee employee = createEmployee("123", "Soumadipta Roy", 50000);
        when(cachedEmployeeService.getEmployeeById("123")).thenReturn(employee);

        mockMvc.perform(get("/api/v1/employee/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.employee_name").value("Soumadipta Roy"));
    }

    @Test
    void getEmployeeById_NotFound() throws Exception {
        when(cachedEmployeeService.getEmployeeById("999"))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(get("/api/v1/employee/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Employee not found"));
    }

    @Test
    void getHighestSalaryOfEmployees_Success() throws Exception {
        when(cachedEmployeeService.getHighestSalaryOfEmployees()).thenReturn(75000);

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string("75000"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() throws Exception {
        List<String> topEarners = Arrays.asList("Vishal Chand", "Somantika Sarkar", "Soumadipta Roy");
        when(cachedEmployeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topEarners);

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Vishal Chand"))
                .andExpect(jsonPath("$[1]").value("Somantika Sarkar"))
                .andExpect(jsonPath("$[2]").value("Soumadipta Roy"));
    }

    @Test
    void createEmployee_Success() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Soumadipta Roy")
                .salary(55000)
                .age(30)
                .title("Developer")
                .build();

        Employee createdEmployee = createEmployee("123", "Soumadipta Roy", 55000);
        when(cachedEmployeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenReturn(createdEmployee);

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.employee_name").value("Soumadipta Roy"))
                .andExpect(jsonPath("$.employee_salary").value(55000));
    }

    @Test
    void createEmployee_ValidationError_MultipleFields() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("")
                .salary(-1000)
                .age(15)
                .title("")
                .build();

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void deleteEmployeeById_Success() throws Exception {
        when(cachedEmployeeService.deleteEmployeeById("123")).thenReturn("Soumadipta Roy");

        mockMvc.perform(delete("/api/v1/employee/123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Soumadipta Roy"));
    }

    @Test
    void deleteEmployeeById_NotFound() throws Exception {
        when(cachedEmployeeService.deleteEmployeeById("999"))
                .thenThrow(new EmployeeNotFoundException("Employee not found"));

        mockMvc.perform(delete("/api/v1/employee/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Employee not found"));
    }

    @Test
    void serviceException_ShouldReturn500() throws Exception {
        when(cachedEmployeeService.getAllEmployees()).thenThrow(new EmployeeServiceException("Service unavailable"));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Service error"))
                .andExpect(jsonPath("$.message").value("Service unavailable"));
    }

    @Test
    void getEmployeesByNameSearch_ServiceException_ShouldReturn500() throws Exception {
        when(cachedEmployeeService.getEmployeesByNameSearch("test"))
                .thenThrow(new EmployeeServiceException("Search service error"));

        mockMvc.perform(get("/api/v1/employee/search/test"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Service error"))
                .andExpect(jsonPath("$.message").value("Search service error"));
    }

    @Test
    void getHighestSalaryOfEmployees_ServiceException_ShouldReturn500() throws Exception {
        when(cachedEmployeeService.getHighestSalaryOfEmployees())
                .thenThrow(new EmployeeServiceException("Salary calculation error"));

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Service error"))
                .andExpect(jsonPath("$.message").value("Salary calculation error"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_ServiceException_ShouldReturn500() throws Exception {
        when(cachedEmployeeService.getTopTenHighestEarningEmployeeNames())
                .thenThrow(new EmployeeServiceException("Top earners calculation error"));

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Service error"))
                .andExpect(jsonPath("$.message").value("Top earners calculation error"));
    }

    @Test
    void createEmployee_ServiceException_ShouldReturn500() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Soumadipta Roy")
                .salary(55000)
                .age(30)
                .title("Developer")
                .build();

        when(cachedEmployeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenThrow(new EmployeeServiceException("Employee creation failed"));

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Service error"))
                .andExpect(jsonPath("$.message").value("Employee creation failed"));
    }
}
