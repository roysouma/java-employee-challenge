package com.reliaquest.api.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for model classes to ensure proper construction and behavior.
 */
class ModelTest {

    @Test
    void employee_BuilderPattern_ShouldCreateEmployee() {
        Employee employee = Employee.builder()
                .id("123")
                .employeeName("Soumadipta Roy")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .employeeEmail("soumadipta.roy@company.com")
                .build();

        assertEquals("123", employee.getId());
        assertEquals("Soumadipta Roy", employee.getEmployeeName());
        assertEquals(50000, employee.getEmployeeSalary());
        assertEquals(30, employee.getEmployeeAge());
        assertEquals("Developer", employee.getEmployeeTitle());
        assertEquals("soumadipta.roy@company.com", employee.getEmployeeEmail());
    }

    @Test
    void employee_SettersAndGetters_ShouldWork() {
        Employee employee = new Employee();
        employee.setId("456");
        employee.setEmployeeName("Somantika Sarkar");
        employee.setEmployeeSalary(60000);
        employee.setEmployeeAge(28);
        employee.setEmployeeTitle("Senior Developer");
        employee.setEmployeeEmail("somantika.srkr@company.com");

        assertEquals("456", employee.getId());
        assertEquals("Somantika Sarkar", employee.getEmployeeName());
        assertEquals(60000, employee.getEmployeeSalary());
        assertEquals(28, employee.getEmployeeAge());
        assertEquals("Senior Developer", employee.getEmployeeTitle());
        assertEquals("somantika.srkr@company.com", employee.getEmployeeEmail());
    }

    @Test
    void createEmployeeRequest_BuilderPattern_ShouldCreateRequest() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Soumadipta Roy")
                .salary(70000)
                .age(32)
                .title("Tech Lead")
                .build();

        assertEquals("Soumadipta Roy", request.getName());
        assertEquals(70000, request.getSalary());
        assertEquals(32, request.getAge());
        assertEquals("Tech Lead", request.getTitle());
    }

    @Test
    void createEmployeeRequest_SettersAndGetters_ShouldWork() {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setName("Soumadipta Roy");
        request.setSalary(55000);
        request.setAge(26);
        request.setTitle("Junior Developer");

        assertEquals("Soumadipta Roy", request.getName());
        assertEquals(55000, request.getSalary());
        assertEquals(26, request.getAge());
        assertEquals("Junior Developer", request.getTitle());
    }

    @Test
    void apiResponse_WithData_ShouldCreateResponse() {
        String data = "test data";
        String status = "success";
        ApiResponse<String> response = new ApiResponse<>(data, status);

        assertEquals(data, response.getData());
        assertEquals(status, response.getStatus());
    }

    @Test
    void apiResponse_SettersAndGetters_ShouldWork() {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setData(42);
        response.setStatus("test status");

        assertEquals(42, response.getData());
        assertEquals("test status", response.getStatus());
    }

    @Test
    void employee_EqualsAndHashCode_ShouldWork() {
        Employee employee1 = Employee.builder()
                .id("123")
                .employeeName("Soumadipta Roy")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .employeeEmail("soumadipta.roy@company.com")
                .build();

        Employee employee2 = Employee.builder()
                .id("123")
                .employeeName("Soumadipta Roy")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Developer")
                .employeeEmail("soumadipta.roy@company.com")
                .build();

        Employee employee3 = Employee.builder()
                .id("456")
                .employeeName("Somantika Sarkar")
                .employeeSalary(60000)
                .employeeAge(28)
                .employeeTitle("Senior Developer")
                .employeeEmail("somantika@company.com")
                .build();

        assertEquals(employee1, employee2);
        assertNotEquals(employee1, employee3);
        assertEquals(employee1.hashCode(), employee2.hashCode());
    }

    @Test
    void employee_ToString_ShouldContainFields() {
        Employee employee = Employee.builder()
                .id("123")
                .employeeName("Soumadipta Roy")
                .employeeSalary(50000)
                .build();
        String toString = employee.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("123"));
        assertTrue(toString.contains("Soumadipta Roy"));
        assertTrue(toString.contains("50000"));
    }
}
