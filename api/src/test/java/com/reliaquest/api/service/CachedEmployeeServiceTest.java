package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

/**
 * Tests for CachedEmployeeService to verify caching behavior.
 */
@SpringBootTest
@TestPropertySource(properties = {"employee.api.base-url=http://localhost:8112/api/v1/employee"})
class CachedEmployeeServiceTest {

    @Autowired
    private CachedEmployeeService cachedEmployeeService;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private CacheManager cacheManager;

    private Employee createEmployee(String id, String name, Integer salary) {
        return Employee.builder()
                .id(id)
                .employeeName(name)
                .employeeSalary(salary)
                .employeeAge(30)
                .employeeTitle("Developer")
                .employeeEmail("test@company.com")
                .build();
    }

    @Test
    void getAllEmployees_CachesResults() {
        List<Employee> employees = Arrays.asList(
                createEmployee("1", "Soumadipta Roy", 50000), createEmployee("2", "Somantika Sarkar", 60000));
        when(employeeService.getAllEmployees()).thenReturn(employees);

        List<Employee> result1 = cachedEmployeeService.getAllEmployees();
        List<Employee> result2 = cachedEmployeeService.getAllEmployees();

        assertEquals(employees, result1);
        assertEquals(employees, result2);

        verify(employeeService, times(1)).getAllEmployees();

        var cache = cacheManager.getCache("allEmployees");
        assertNotNull(cache);
        assertNotNull(cache.get("all"));
    }

    @Test
    void getEmployeeById_CachesResults() {
        Employee employee = createEmployee("123", "Soumadipta Roy", 50000);
        when(employeeService.getEmployeeById("123")).thenReturn(employee);

        Employee result1 = cachedEmployeeService.getEmployeeById("123");
        Employee result2 = cachedEmployeeService.getEmployeeById("123");

        assertEquals(employee, result1);
        assertEquals(employee, result2);

        verify(employeeService, times(1)).getEmployeeById("123");

        var cache = cacheManager.getCache("employeeById");
        assertNotNull(cache);
        assertNotNull(cache.get("123"));
    }

    @Test
    void getEmployeesByNameSearch_CachesResults() {
        List<Employee> employees = Arrays.asList(createEmployee("1", "Soumadipta Roy", 50000));
        when(employeeService.getEmployeesByNameSearch("Soumadipta")).thenReturn(employees);

        List<Employee> result1 = cachedEmployeeService.getEmployeesByNameSearch("Soumadipta");
        List<Employee> result2 = cachedEmployeeService.getEmployeesByNameSearch("Soumadipta");

        assertEquals(employees, result1);
        assertEquals(employees, result2);

        verify(employeeService, times(1)).getEmployeesByNameSearch("Soumadipta");
    }

    @Test
    void getHighestSalaryOfEmployees_CachesResults() {
        Integer highestSalary = 100000;
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(highestSalary);

        Integer result1 = cachedEmployeeService.getHighestSalaryOfEmployees();
        Integer result2 = cachedEmployeeService.getHighestSalaryOfEmployees();

        assertEquals(highestSalary, result1);
        assertEquals(highestSalary, result2);

        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_CachesResults() {
        List<String> topEarners = Arrays.asList("Soumadipta Roy", "Somantika Sarkar");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topEarners);

        List<String> result1 = cachedEmployeeService.getTopTenHighestEarningEmployeeNames();
        List<String> result2 = cachedEmployeeService.getTopTenHighestEarningEmployeeNames();

        assertEquals(topEarners, result1);
        assertEquals(topEarners, result2);

        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void createEmployee_EvictsCaches() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("Vishal Chand")
                .salary(75000)
                .age(28)
                .title("Developer")
                .build();
        Employee createdEmployee = createEmployee("123", "Vishal Chand", 75000);
        when(employeeService.createEmployee(request)).thenReturn(createdEmployee);

        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(createdEmployee));
        cachedEmployeeService.getAllEmployees();

        var cache = cacheManager.getCache("allEmployees");
        assertNotNull(cache);
        assertNotNull(cache.get("all"));

        Employee result = cachedEmployeeService.createEmployee(request);

        assertEquals(createdEmployee, result);
        assertNull(cache.get("all"));
    }

    @Test
    void deleteEmployeeById_EvictsCaches() {
        String employeeId = "123";
        String employeeName = "Soumadipta Roy";
        when(employeeService.deleteEmployeeById(employeeId)).thenReturn(employeeName);

        Employee employee = createEmployee(employeeId, employeeName, 50000);
        when(employeeService.getEmployeeById(employeeId)).thenReturn(employee);
        cachedEmployeeService.getEmployeeById(employeeId);

        var cache = cacheManager.getCache("employeeById");
        assertNotNull(cache);
        assertNotNull(cache.get(employeeId));

        String result = cachedEmployeeService.deleteEmployeeById(employeeId);

        assertEquals(employeeName, result);
        assertNull(cache.get(employeeId));
    }
}
