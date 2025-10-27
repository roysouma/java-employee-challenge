package com.reliaquest.api.config;

import java.lang.reflect.Method;
import java.util.Arrays;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

/**
 * Cache setup - using Caffeine for in-memory caching.
 */
@Configuration
@EnableCaching
@NoArgsConstructor
@Slf4j
public class CacheConfig {

    public static final String ALL_EMPLOYEES_CACHE = "allEmployees";
    public static final String EMPLOYEE_BY_ID_CACHE = "employeeById";
    public static final String EMPLOYEE_SEARCH_CACHE = "employeeSearch";
    public static final String SALARY_CALCULATIONS_CACHE = "salaryCalculations";

    /**
     * Custom key generator which creates keys based on method name and parameters.
     */
    @Bean("searchKeyGenerator")
    public KeyGenerator searchKeyGenerator() {
        return new KeyGenerator() {
            @Override
            @NonNull public Object generate(@NonNull Object target, @NonNull Method method, @NonNull Object... params) {
                StringBuilder keyBuilder = new StringBuilder();
                keyBuilder.append(method.getName());
                if (params != null && params.length > 0) {
                    keyBuilder.append(":");
                    keyBuilder.append(Arrays.toString(params));
                }
                return keyBuilder.toString();
            }
        };
    }
}
