package com.reliaquest.api.service;

import com.reliaquest.api.config.CacheConfig;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

/**
 * Wrapper around EmployeeService that adds caching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CachedEmployeeService {

    private final EmployeeService employeeService;

    @Cacheable(value = CacheConfig.ALL_EMPLOYEES_CACHE, key = "'all'")
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @Cacheable(value = CacheConfig.EMPLOYEE_BY_ID_CACHE, key = "#id")
    public Employee getEmployeeById(String id) {
        return employeeService.getEmployeeById(id);
    }

    @Cacheable(value = CacheConfig.EMPLOYEE_SEARCH_CACHE, keyGenerator = "searchKeyGenerator")
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        return employeeService.getEmployeesByNameSearch(searchString);
    }

    @Cacheable(value = CacheConfig.SALARY_CALCULATIONS_CACHE, key = "'highestSalary'")
    public Integer getHighestSalaryOfEmployees() {
        return employeeService.getHighestSalaryOfEmployees();
    }

    @Cacheable(value = CacheConfig.SALARY_CALCULATIONS_CACHE, key = "'topTenEarners'")
    public List<String> getTopTenHighestEarningEmployeeNames() {
        return employeeService.getTopTenHighestEarningEmployeeNames();
    }

    @Caching(
            evict = {
                @CacheEvict(value = CacheConfig.ALL_EMPLOYEES_CACHE, allEntries = true),
                @CacheEvict(value = CacheConfig.SALARY_CALCULATIONS_CACHE, allEntries = true),
                @CacheEvict(value = CacheConfig.EMPLOYEE_SEARCH_CACHE, allEntries = true)
            })
    public Employee createEmployee(CreateEmployeeRequest request) {
        return employeeService.createEmployee(request);
    }

    @Caching(
            evict = {
                @CacheEvict(value = CacheConfig.ALL_EMPLOYEES_CACHE, allEntries = true),
                @CacheEvict(value = CacheConfig.EMPLOYEE_BY_ID_CACHE, key = "#id"),
                @CacheEvict(value = CacheConfig.SALARY_CALCULATIONS_CACHE, allEntries = true),
                @CacheEvict(value = CacheConfig.EMPLOYEE_SEARCH_CACHE, allEntries = true)
            })
    public String deleteEmployeeById(String id) {
        return employeeService.deleteEmployeeById(id);
    }
}
