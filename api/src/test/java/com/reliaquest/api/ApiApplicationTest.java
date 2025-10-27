package com.reliaquest.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for the Spring Boot application context.
 * Verifies that the application starts up correctly and all beans are properly
 * configured.
 */
@SpringBootTest
class ApiApplicationTest {

    /**
     * Test that the Spring Boot application context loads successfully.
     * This test ensures that:
     * - All configuration classes are valid
     * - All beans can be created without circular dependencies
     * - The application can start up without errors
     */
    @Test
    void contextLoads() {
        // This test will pass if the Spring context loads successfully
        // No additional assertions needed - context loading is the test
    }
}
