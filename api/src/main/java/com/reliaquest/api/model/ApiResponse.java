package com.reliaquest.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic wrapper for API responses from the mock employee service.
 *
 * @param <T> The type of data contained in the response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    // The actual data payload of the response
    private T data;

    private String status;
}
