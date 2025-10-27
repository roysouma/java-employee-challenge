package com.reliaquest.api.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests that require the mock server to be running.
 * Run with: -Dmock.server.running=true
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"employee.api.base-url=http://localhost:8112/api/v1/employee"})
@EnabledIfSystemProperty(named = "mock.server.running", matches = "true")
class EmployeeWithMockServerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/employee";
    }

    @Test
    void getAllEmployees_WithMockServer_ReturnsEmployeeList() {
        ResponseEntity<List<Employee>> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<List<Employee>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());

        Employee firstEmployee = response.getBody().get(0);
        assertNotNull(firstEmployee.getId());
        assertNotNull(firstEmployee.getEmployeeName());
        assertNotNull(firstEmployee.getEmployeeSalary());
        assertNotNull(firstEmployee.getEmployeeAge());
        assertNotNull(firstEmployee.getEmployeeTitle());
        assertNotNull(firstEmployee.getEmployeeEmail());
    }

    @Test
    void getEmployeeById_WithValidId_ReturnsEmployee() {
        ResponseEntity<List<Employee>> allEmployeesResponse = restTemplate.exchange(
                getBaseUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<List<Employee>>() {});

        assertTrue(allEmployeesResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(allEmployeesResponse.getBody());
        assertFalse(allEmployeesResponse.getBody().isEmpty());

        String validId = allEmployeesResponse.getBody().get(0).getId();
        ResponseEntity<Employee> response = restTemplate.getForEntity(getBaseUrl() + "/" + validId, Employee.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(validId, response.getBody().getId());
    }

    @Test
    void getEmployeesByNameSearch_WithValidName_ReturnsFilteredList() {
        ResponseEntity<List<Employee>> allEmployeesResponse = restTemplate.exchange(
                getBaseUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<List<Employee>>() {});

        assertTrue(allEmployeesResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(allEmployeesResponse.getBody());
        assertFalse(allEmployeesResponse.getBody().isEmpty());

        String searchName =
                allEmployeesResponse.getBody().get(0).getEmployeeName().substring(0, 3);

        ResponseEntity<List<Employee>> response = restTemplate.exchange(
                getBaseUrl() + "/search/" + searchName,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Employee>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        response.getBody()
                .forEach(employee ->
                        assertTrue(employee.getEmployeeName().toLowerCase().contains(searchName.toLowerCase())));
    }

    @Test
    void getHighestSalary_WithMockServer_ReturnsValidSalary() {
        ResponseEntity<Integer> response = restTemplate.getForEntity(getBaseUrl() + "/highestSalary", Integer.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() > 0);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_WithMockServer_ReturnsNamesList() {
        ResponseEntity<List<String>> response = restTemplate.exchange(
                getBaseUrl() + "/topTenHighestEarningEmployeeNames",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() <= 10);

        response.getBody().forEach(name -> {
            assertNotNull(name);
            assertFalse(name.trim().isEmpty());
        });
    }

    @Test
    void createEmployee_WithValidData_CreatesAndReturnsEmployee() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Soumadipta Roy")
                .salary(50000)
                .age(30)
                .title("Developer")
                .build();

        ResponseEntity<Employee> response = restTemplate.postForEntity(getBaseUrl(), request, Employee.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(request.getName(), response.getBody().getEmployeeName());
        assertEquals(request.getSalary(), response.getBody().getEmployeeSalary());
        assertEquals(request.getAge(), response.getBody().getEmployeeAge());
        assertEquals(request.getTitle(), response.getBody().getEmployeeTitle());
        assertNotNull(response.getBody().getId());
        assertNotNull(response.getBody().getEmployeeEmail());
    }

    @Test
    void deleteEmployee_WithValidId_DeletesAndReturnsName() {
        CreateEmployeeRequest createRequest = CreateEmployeeRequest.builder()
                .name("Soumadipta Roy")
                .salary(45000)
                .age(25)
                .title("Temporary Employee")
                .build();

        ResponseEntity<Employee> createResponse =
                restTemplate.postForEntity(getBaseUrl(), createRequest, Employee.class);

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        String employeeId = createResponse.getBody().getId();
        String employeeName = createResponse.getBody().getEmployeeName();

        ResponseEntity<String> deleteResponse =
                restTemplate.exchange(getBaseUrl() + "/" + employeeId, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertEquals(employeeName, deleteResponse.getBody());
    }

    @Test
    void createEmployee_WithInvalidData_ReturnsBadRequest() {
        CreateEmployeeRequest invalidRequest = CreateEmployeeRequest.builder()
                .name("")
                .salary(-1000)
                .age(15)
                .title("")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl(), invalidRequest, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Validation failed"));
    }

    @Test
    void fullWorkflow_CreateSearchUpdateDelete_WorksEndToEnd() {
        CreateEmployeeRequest createRequest = CreateEmployeeRequest.builder()
                .name("Soumadipta Roy")
                .salary(60000)
                .age(35)
                .title("Developer")
                .build();

        ResponseEntity<Employee> createResponse =
                restTemplate.postForEntity(getBaseUrl(), createRequest, Employee.class);
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        String employeeId = createResponse.getBody().getId();

        String searchTerm = "Soumadipta";
        ResponseEntity<List<Employee>> searchResponse = restTemplate.exchange(
                getBaseUrl() + "/search/" + searchTerm,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Employee>>() {});
        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        assertNotNull(searchResponse.getBody());
        assertTrue(searchResponse.getBody().stream().anyMatch(emp -> emp.getId().equals(employeeId)));

        ResponseEntity<Employee> getResponse =
                restTemplate.getForEntity(getBaseUrl() + "/" + employeeId, Employee.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(employeeId, getResponse.getBody().getId());

        ResponseEntity<String> deleteResponse =
                restTemplate.exchange(getBaseUrl() + "/" + employeeId, HttpMethod.DELETE, null, String.class);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertEquals("Soumadipta Roy", deleteResponse.getBody());
    }
}
