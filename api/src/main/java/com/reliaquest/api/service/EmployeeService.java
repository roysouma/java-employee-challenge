package com.reliaquest.api.service;

import com.reliaquest.api.config.CacheConfig;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Employee service - talks to the mock API server.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final RestTemplate restTemplate;

    @Value("${employee.api.base-url:http://localhost:8112/api/v1/employee}")
    private String baseUrl;

    @Cacheable(CacheConfig.ALL_EMPLOYEES_CACHE)
    @Retryable(
            retryFor = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 8,
            backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 20000))
    public List<Employee> getAllEmployees() {
        try {
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Employee> employees = response.getBody().getData();
                log.debug("Got {} employees from API", employees != null ? employees.size() : 0);
                return employees != null ? employees : Collections.emptyList();
            }

            throw new EmployeeServiceException("API returned: " + response.getStatusCode());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("Rate limited, retrying...");
                throw e;
            }
            throw new EmployeeServiceException("API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to get employees: {}", e.getMessage());
            throw new EmployeeServiceException("Failed to fetch employees", e);
        }
    }

    @Cacheable(value = CacheConfig.EMPLOYEE_SEARCH_CACHE, keyGenerator = "searchKeyGenerator")
    @Retryable(
            retryFor = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 8,
            backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 20000))
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        List<Employee> allEmployees = getAllEmployees();
        List<Employee> results = new ArrayList<>();
        for (Employee emp : allEmployees) {
            if (emp.getEmployeeName() != null
                    && emp.getEmployeeName().toLowerCase().contains(searchString.toLowerCase())) {
                results.add(emp);
            }
        }
        return results;
    }

    @Cacheable(CacheConfig.EMPLOYEE_BY_ID_CACHE)
    @Retryable(
            retryFor = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 8,
            backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 20000))
    public Employee getEmployeeById(String id) {
        try {
            String url = baseUrl + "/" + id;
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Employee employee = response.getBody().getData();
                if (employee != null) {
                    return employee;
                }
            }
            throw new EmployeeNotFoundException("Employee not found: " + id);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new EmployeeNotFoundException("Employee not found: " + id);
            }
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw e;
            }
            throw new EmployeeServiceException("API error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new EmployeeServiceException("Failed to get employee", e);
        }
    }

    @Cacheable(value = CacheConfig.SALARY_CALCULATIONS_CACHE, key = "'highestSalary'")
    @Retryable(
            retryFor = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 8,
            backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 20000))
    public Integer getHighestSalaryOfEmployees() {
        List<Employee> employees = getAllEmployees();
        if (employees.isEmpty()) {
            return 0;
        }

        return employees.stream()
                .filter(emp -> emp.getEmployeeSalary() != null)
                .mapToInt(Employee::getEmployeeSalary)
                .max()
                .orElse(0);
    }

    @Cacheable(value = CacheConfig.SALARY_CALCULATIONS_CACHE, key = "'topTenEarners'")
    @Retryable(
            retryFor = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 8,
            backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 20000))
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return getAllEmployees().stream()
                .filter(emp -> emp.getEmployeeSalary() != null && emp.getEmployeeName() != null)
                .sorted(Comparator.comparing(Employee::getEmployeeSalary, Comparator.reverseOrder()))
                .limit(10)
                .map(Employee::getEmployeeName)
                .collect(Collectors.toList());
    }

    @CacheEvict(
            value = {CacheConfig.ALL_EMPLOYEES_CACHE, CacheConfig.SALARY_CALCULATIONS_CACHE},
            allEntries = true)
    @Retryable(
            retryFor = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 8,
            backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 20000))
    public Employee createEmployee(CreateEmployeeRequest request) {
        try {
            HttpEntity<CreateEmployeeRequest> entity = new HttpEntity<>(request);
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Employee employee = response.getBody().getData();
                if (employee != null) {
                    log.info("Created employee: {} ({})", employee.getEmployeeName(), employee.getId());
                    return employee;
                }
            }
            throw new EmployeeServiceException("Employee creation failed");
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw e;
            }
            throw new EmployeeServiceException("Create failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new EmployeeServiceException("Failed to create employee", e);
        }
    }

    @CacheEvict(
            value = {
                CacheConfig.ALL_EMPLOYEES_CACHE,
                CacheConfig.EMPLOYEE_BY_ID_CACHE,
                CacheConfig.SALARY_CALCULATIONS_CACHE
            },
            allEntries = true)
    @Retryable(
            retryFor = {HttpServerErrorException.class, HttpClientErrorException.TooManyRequests.class},
            maxAttempts = 8,
            backoff = @Backoff(delay = 2000, multiplier = 2, random = true, maxDelay = 20000))
    public String deleteEmployeeById(String id) {
        Employee employee = getEmployeeById(id);
        String employeeName = employee.getEmployeeName();

        try {
            Map<String, String> deleteRequest = Map.of("name", employeeName);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(deleteRequest);

            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.DELETE, entity, new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean deleted = response.getBody().getData();
                if (Boolean.TRUE.equals(deleted)) {
                    return employeeName;
                }
            }
            throw new EmployeeServiceException("Couldn't delete " + employeeName);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new EmployeeNotFoundException("Employee not found: " + employeeName);
            }
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw e;
            }
            throw new EmployeeServiceException("Delete failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new EmployeeServiceException("Failed to delete employee", e);
        }
    }
}
