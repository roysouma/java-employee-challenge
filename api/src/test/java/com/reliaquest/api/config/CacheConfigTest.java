package com.reliaquest.api.config;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;

/**
 * Tests for CacheConfig to ensure proper cache configuration and key generation.
 */
@ExtendWith(MockitoExtension.class)
class CacheConfigTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private CacheConfig cacheConfig;

    @BeforeEach
    void setUp() {
        cacheConfig = new CacheConfig();
    }

    @Test
    void searchKeyGenerator_WithMethodAndParams_ShouldGenerateCorrectKey() throws NoSuchMethodException {
        KeyGenerator keyGenerator = cacheConfig.searchKeyGenerator();
        Method method = String.class.getMethod("substring", int.class);
        Object target = new Object();
        Object[] params = {5, "test"};
        Object key = keyGenerator.generate(target, method, params);

        assertNotNull(key);
        assertTrue(key.toString().contains("substring"));
        assertTrue(key.toString().contains("[5, test]"));
    }

    @Test
    void searchKeyGenerator_WithMethodNoParams_ShouldGenerateCorrectKey() throws NoSuchMethodException {
        KeyGenerator keyGenerator = cacheConfig.searchKeyGenerator();
        Method method = String.class.getMethod("length");
        Object target = new Object();
        Object key = keyGenerator.generate(target, method);

        assertNotNull(key);
        assertEquals("length", key.toString());
    }

    @Test
    void searchKeyGenerator_WithNullParams_ShouldGenerateCorrectKey() throws NoSuchMethodException {
        KeyGenerator keyGenerator = cacheConfig.searchKeyGenerator();
        Method method = String.class.getMethod("length");
        Object target = new Object();
        Object key = keyGenerator.generate(target, method);

        assertNotNull(key);
        assertEquals("length", key.toString());
    }

    @Test
    void cacheConstants_ShouldHaveCorrectValues() {
        assertEquals("allEmployees", CacheConfig.ALL_EMPLOYEES_CACHE);
        assertEquals("employeeById", CacheConfig.EMPLOYEE_BY_ID_CACHE);
        assertEquals("employeeSearch", CacheConfig.EMPLOYEE_SEARCH_CACHE);
        assertEquals("salaryCalculations", CacheConfig.SALARY_CALCULATIONS_CACHE);
    }
}
