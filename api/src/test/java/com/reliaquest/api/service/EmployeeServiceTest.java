package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeService employeeService;

    private final String baseUrl = "http://localhost:8112/api/v1/employee";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(employeeService, "baseUrl", baseUrl);
    }

    @Test
    void getAllEmployees_Success() {
        List<Employee> employees = Arrays.asList(
                createEmployee("1", "Soumadipta Roy", 50000), createEmployee("2", "Somantika Sarkar", 60000));
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
        assertEquals("Soumadipta Roy", result.get(0).getEmployeeName());
        assertEquals("Somantika Sarkar", result.get(1).getEmployeeName());
        verify(restTemplate).exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class));
    }

    @Test
    void getAllEmployees_EmptyResponse() {
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(null, "Success");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<Employee> result = employeeService.getAllEmployees();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllEmployees_TooManyRequests_ThrowsException() {
        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        assertThrows(HttpClientErrorException.class, () -> employeeService.getAllEmployees());
    }

    @Test
    void getEmployeesByNameSearch_Success() {
        List<Employee> employees = Arrays.asList(
                createEmployee("1", "Soumadipta Roy", 50000),
                createEmployee("2", "Somantika Sarkar", 60000),
                createEmployee("3", "Vishal Chand", 55000));
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<Employee> result = employeeService.getEmployeesByNameSearch("Soumadipta");

        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(emp -> emp.getEmployeeName().contains("Soumadipta")));
    }

    @Test
    void getEmployeesByNameSearch_CaseInsensitive() {
        List<Employee> employees = Arrays.asList(createEmployee("1", "Soumadipta Roy", 50000));
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<Employee> result = employeeService.getEmployeesByNameSearch("soumadipta");

        assertEquals(1, result.size());
        assertEquals("Soumadipta Roy", result.get(0).getEmployeeName());
    }

    @Test
    void getEmployeeById_Success() {
        String employeeId = "123";
        Employee employee = createEmployee(employeeId, "Soumadipta Roy", 50000);
        ApiResponse<Employee> apiResponse = new ApiResponse<>(employee, "Success");
        ResponseEntity<ApiResponse<Employee>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq(baseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        Employee result = employeeService.getEmployeeById(employeeId);

        assertEquals(employeeId, result.getId());
        assertEquals("Soumadipta Roy", result.getEmployeeName());
    }

    @Test
    void getEmployeeById_NotFound_ThrowsException() {
        String employeeId = "999";
        when(restTemplate.exchange(
                        eq(baseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        EmployeeNotFoundException exception =
                assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(employeeId));
        assertTrue(exception.getMessage().contains(employeeId));
    }

    @Test
    void getHighestSalaryOfEmployees_Success() {
        List<Employee> employees = Arrays.asList(
                createEmployee("1", "Soumadipta Roy", 50000),
                createEmployee("2", "Somantika Sarkar", 75000),
                createEmployee("3", "Vishal Chand", 60000));
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        Integer result = employeeService.getHighestSalaryOfEmployees();

        assertEquals(75000, result);
    }

    @Test
    void getHighestSalaryOfEmployees_EmptyList_ReturnsZero() {
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(Collections.emptyList(), "Success");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        Integer result = employeeService.getHighestSalaryOfEmployees();

        assertEquals(0, result);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_Success() {
        List<Employee> employees = Arrays.asList(
                createEmployee("1", "Soumadipta Roy", 50000),
                createEmployee("2", "Somantika Sarkar", 75000),
                createEmployee("3", "Vishal Chand", 60000),
                createEmployee("4", "Rinku Maurya", 80000));
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        assertEquals(4, result.size());
        assertEquals("Rinku Maurya", result.get(0));
        assertEquals("Somantika Sarkar", result.get(1));
        assertEquals("Vishal Chand", result.get(2));
        assertEquals("Soumadipta Roy", result.get(3));
    }

    @Test
    void createEmployee_Success() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Soumadipta Roy")
                .salary(55000)
                .age(30)
                .title("Developer")
                .build();

        Employee createdEmployee = createEmployee("123", "Soumadipta Roy", 55000);
        ApiResponse<Employee> apiResponse = new ApiResponse<>(createdEmployee, "Success");
        ResponseEntity<ApiResponse<Employee>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq(baseUrl), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        Employee result = employeeService.createEmployee(request);

        assertEquals("123", result.getId());
        assertEquals("Soumadipta Roy", result.getEmployeeName());
        assertEquals(55000, result.getEmployeeSalary());
    }

    @Test
    void deleteEmployeeById_Success() {
        String employeeId = "123";
        String employeeName = "Soumadipta Roy";
        Employee employee = createEmployee(employeeId, employeeName, 50000);

        ApiResponse<Employee> getResponse = new ApiResponse<>(employee, "Success");
        ResponseEntity<ApiResponse<Employee>> getResponseEntity = new ResponseEntity<>(getResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq(baseUrl + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(getResponseEntity);

        ApiResponse<Boolean> deleteResponse = new ApiResponse<>(true, "Success");
        ResponseEntity<ApiResponse<Boolean>> deleteResponseEntity = new ResponseEntity<>(deleteResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                        eq(baseUrl),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(deleteResponseEntity);

        String result = employeeService.deleteEmployeeById(employeeId);

        assertEquals(employeeName, result);
    }

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
    void getEmployeesByNameSearch_WithNullEmployeeName_FiltersCorrectly() {
        List<Employee> employees = Arrays.asList(
                createEmployee("1", "Soumadipta Roy", 50000),
                Employee.builder()
                        .id("2")
                        .employeeName(null)
                        .employeeSalary(60000)
                        .build(),
                createEmployee("3", "Somantika Sarkar", 70000));
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(employees, "Success");
        ResponseEntity<ApiResponse<List<Employee>>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.exchange(eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<Employee> result = employeeService.getEmployeesByNameSearch("Soumadipta");

        assertEquals(1, result.size());
        assertEquals("Soumadipta Roy", result.get(0).getEmployeeName());
    }

    @Test
    void deleteEmployeeById_FailedDeleteOperation_ThrowsEmployeeServiceException() {
        setupDeleteEmployeeGetCall();

        ApiResponse<Boolean> deleteResponse = new ApiResponse<>(false, "Failed");
        ResponseEntity<ApiResponse<Boolean>> deleteResponseEntity = ResponseEntity.ok(deleteResponse);
        when(restTemplate.exchange(
                        eq(baseUrl),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(deleteResponseEntity);

        assertThrows(EmployeeServiceException.class, () -> employeeService.deleteEmployeeById("123"));
    }

    @Test
    void deleteEmployeeById_NullDeleteResponse_ThrowsEmployeeServiceException() {
        setupDeleteEmployeeGetCall();

        ResponseEntity<ApiResponse<Boolean>> deleteResponseEntity = ResponseEntity.ok(null);
        when(restTemplate.exchange(
                        eq(baseUrl),
                        eq(HttpMethod.DELETE),
                        any(HttpEntity.class),
                        any(ParameterizedTypeReference.class)))
                .thenReturn(deleteResponseEntity);

        assertThrows(EmployeeServiceException.class, () -> employeeService.deleteEmployeeById("123"));
    }

    // ========== PARAMETERIZED ERROR HANDLING TESTS ==========

    @ParameterizedTest(name = "{0} with HttpClientErrorException should throw EmployeeServiceException")
    @MethodSource("errorScenarios")
    void httpClientError_ThrowsEmployeeServiceException(String method) {
        testErrorScenario(method, new HttpClientErrorException(HttpStatus.BAD_REQUEST));
    }

    @ParameterizedTest(name = "{0} with HttpServerErrorException should throw EmployeeServiceException")
    @MethodSource("serverErrorScenarios")
    void httpServerError_ThrowsEmployeeServiceException(String method) {
        testErrorScenario(method, new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ParameterizedTest(name = "{0} with RuntimeException should throw EmployeeServiceException")
    @MethodSource("errorScenarios")
    void unexpectedError_ThrowsEmployeeServiceException(String method) {
        testErrorScenario(method, new RuntimeException("Network error"));
    }

    @ParameterizedTest(name = "{0} - {1} should throw EmployeeServiceException")
    @MethodSource("invalidResponseScenarios")
    void invalidResponse_ThrowsEmployeeServiceException(String method, String responseType) {
        ResponseEntity<?> response = "null".equals(responseType)
                ? ResponseEntity.ok(null)
                : ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
        testErrorScenario(method, response);
    }

    static Stream<String> errorScenarios() {
        return Stream.of("getAllEmployees", "getEmployeeById", "createEmployee", "deleteEmployeeById");
    }

    static Stream<String> serverErrorScenarios() {
        return Stream.of("getAllEmployees", "createEmployee", "deleteEmployeeById");
    }

    static Stream<Arguments> invalidResponseScenarios() {
        return Stream.of(
                Arguments.of("getAllEmployees", "null"),
                Arguments.of("getAllEmployees", "status"),
                Arguments.of("getEmployeeById", "null"),
                Arguments.of("getEmployeeById", "status"),
                Arguments.of("createEmployee", "null"),
                Arguments.of("createEmployee", "status"));
    }

    private void testErrorScenario(String method, Exception exception) {
        switch (method) {
            case "getAllEmployees":
                when(restTemplate.exchange(
                                eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                        .thenThrow(exception);
                assertThrows(EmployeeServiceException.class, () -> employeeService.getAllEmployees());
                break;
            case "getEmployeeById":
                when(restTemplate.exchange(
                                eq(baseUrl + "/123"),
                                eq(HttpMethod.GET),
                                isNull(),
                                any(ParameterizedTypeReference.class)))
                        .thenThrow(exception);
                assertThrows(EmployeeServiceException.class, () -> employeeService.getEmployeeById("123"));
                break;
            case "createEmployee":
                when(restTemplate.exchange(
                                eq(baseUrl),
                                eq(HttpMethod.POST),
                                any(HttpEntity.class),
                                any(ParameterizedTypeReference.class)))
                        .thenThrow(exception);
                assertThrows(
                        EmployeeServiceException.class, () -> employeeService.createEmployee(createEmployeeRequest()));
                break;
            case "deleteEmployeeById":
                setupDeleteEmployeeGetCall();
                when(restTemplate.exchange(
                                eq(baseUrl),
                                eq(HttpMethod.DELETE),
                                any(HttpEntity.class),
                                any(ParameterizedTypeReference.class)))
                        .thenThrow(exception);
                assertThrows(EmployeeServiceException.class, () -> employeeService.deleteEmployeeById("123"));
                break;
        }
    }

    private void testErrorScenario(String method, ResponseEntity<?> response) {
        switch (method) {
            case "getAllEmployees":
                when(restTemplate.exchange(
                                eq(baseUrl), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                        .thenReturn(response);
                assertThrows(EmployeeServiceException.class, () -> employeeService.getAllEmployees());
                break;
            case "getEmployeeById":
                when(restTemplate.exchange(
                                eq(baseUrl + "/123"),
                                eq(HttpMethod.GET),
                                isNull(),
                                any(ParameterizedTypeReference.class)))
                        .thenReturn(response);
                assertThrows(EmployeeServiceException.class, () -> employeeService.getEmployeeById("123"));
                break;
            case "createEmployee":
                when(restTemplate.exchange(
                                eq(baseUrl),
                                eq(HttpMethod.POST),
                                any(HttpEntity.class),
                                any(ParameterizedTypeReference.class)))
                        .thenReturn(response);
                assertThrows(
                        EmployeeServiceException.class, () -> employeeService.createEmployee(createEmployeeRequest()));
                break;
        }
    }

    private CreateEmployeeRequest createEmployeeRequest() {
        return CreateEmployeeRequest.builder()
                .name("Soumadipta Roy")
                .salary(50000)
                .age(30)
                .title("Developer")
                .build();
    }

    private void setupDeleteEmployeeGetCall() {
        Employee employee = createEmployee("123", "Soumadipta Roy", 50000);
        ApiResponse<Employee> getResponse = new ApiResponse<>(employee, "Success");
        ResponseEntity<ApiResponse<Employee>> getResponseEntity = ResponseEntity.ok(getResponse);

        when(restTemplate.exchange(
                        eq(baseUrl + "/123"), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(getResponseEntity);
    }
}
