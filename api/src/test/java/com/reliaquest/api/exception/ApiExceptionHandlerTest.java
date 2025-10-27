package com.reliaquest.api.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Tests for ApiExceptionHandler to ensure proper exception handling and response formatting.
 */
class ApiExceptionHandlerTest {

    private ApiExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ApiExceptionHandler();
    }

    @Test
    void handleEmployeeNotFound_ShouldReturn404WithErrorMessage() {
        String errorMessage = "Employee not found with ID: 123";
        EmployeeNotFoundException exception = new EmployeeNotFoundException(errorMessage);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleEmployeeNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Employee not found", response.getBody().get("error"));
        assertEquals(errorMessage, response.getBody().get("message"));
    }

    @Test
    void handleEmployeeServiceException_ShouldReturn500WithErrorMessage() {
        String errorMessage = "Service unavailable";
        EmployeeServiceException exception = new EmployeeServiceException(errorMessage);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleEmployeeServiceException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Service error", response.getBody().get("error"));
        assertEquals(errorMessage, response.getBody().get("message"));
    }

    @Test
    void handleValidationExceptions_ShouldReturn400WithValidationErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "name", "Name is required"));
        bindingResult.addError(new FieldError("testObject", "salary", "Salary must be positive"));

        MethodParameter mockParameter = mock(MethodParameter.class);
        when(mockParameter.getParameterIndex()).thenReturn(0);
        when(mockParameter.getExecutable()).thenReturn(null); // This can be null for our test

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(mockParameter, bindingResult);
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().get("error"));

        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertNotNull(details);
        assertEquals("Name is required", details.get("name"));
        assertEquals("Salary must be positive", details.get("salary"));
    }

    @Test
    void handleGenericException_ShouldReturn500WithGenericMessage() {
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().get("error"));
        assertEquals("Something went wrong", response.getBody().get("message"));
    }

    @Test
    void handleGenericException_WithNullPointerException_ShouldReturn500() {
        NullPointerException exception = new NullPointerException("Null value encountered");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().get("error"));
        assertEquals("Something went wrong", response.getBody().get("message"));
    }
}
