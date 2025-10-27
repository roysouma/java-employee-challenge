package com.reliaquest.api.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for custom exception classes to ensure proper construction and behavior.
 */
class ExceptionTest {

    @Test
    void employeeNotFoundException_WithMessage_ShouldCreateException() {
        String message = "Employee not found with ID: 123";
        EmployeeNotFoundException exception = new EmployeeNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void employeeNotFoundException_WithMessageAndCause_ShouldCreateException() {
        String message = "Employee not found with ID: 123";
        Throwable cause = new RuntimeException("Network error");

        EmployeeNotFoundException exception = new EmployeeNotFoundException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void employeeServiceException_WithMessage_ShouldCreateException() {
        String message = "Service unavailable";
        EmployeeServiceException exception = new EmployeeServiceException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void employeeServiceException_WithMessageAndCause_ShouldCreateException() {
        String message = "Service unavailable";
        Throwable cause = new RuntimeException("Connection timeout");

        EmployeeServiceException exception = new EmployeeServiceException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
