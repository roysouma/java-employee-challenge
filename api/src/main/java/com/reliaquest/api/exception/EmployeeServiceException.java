package com.reliaquest.api.exception;

/**
 * Exception thrown when there's an error in the employee service layer.
 */
public class EmployeeServiceException extends RuntimeException {

    public EmployeeServiceException(String message) {
        super(message);
    }

    public EmployeeServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
