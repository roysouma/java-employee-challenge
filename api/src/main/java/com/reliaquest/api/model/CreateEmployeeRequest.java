package com.reliaquest.api.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for creating new employees.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEmployeeRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Positive(message = "Salary must be greater than zero") @NotNull(message = "Salary is required") private Integer salary;

    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    @NotNull(message = "Age is required") private Integer age;

    @NotBlank(message = "Title is required")
    private String title;
}
