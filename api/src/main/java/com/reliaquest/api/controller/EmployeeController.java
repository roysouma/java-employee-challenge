package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.CachedEmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Employee REST controller - handles all employee-related HTTP requests.
 */
@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeRequest> {

    private final CachedEmployeeService cachedEmployeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = cachedEmployeeService.getAllEmployees();
        log.debug("Returning {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        return ResponseEntity.ok(cachedEmployeeService.getEmployeesByNameSearch(searchString));
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        Employee employee = cachedEmployeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        Integer salary = cachedEmployeeService.getHighestSalaryOfEmployees();
        return ResponseEntity.ok(salary);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(cachedEmployeeService.getTopTenHighestEarningEmployeeNames());
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@Valid CreateEmployeeRequest employeeInput) {
        log.info("Creating new employee: {}", employeeInput.getName());
        Employee employee = cachedEmployeeService.createEmployee(employeeInput);
        return ResponseEntity.ok(employee);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        String employeeName = cachedEmployeeService.deleteEmployeeById(id);
        log.info("Deleted employee: {}", employeeName);
        return ResponseEntity.ok(employeeName);
    }
}
