package com.reliaquest.api.exception;

/**
 * Exception thrown when an employee cannot be found by the specified criteria.
 */
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(String message) {
        super(message);
    }

    public EmployeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
