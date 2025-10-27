package com.reliaquest.api.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Tests for RetryConfig to ensure proper retry configuration.
 */
class RetryConfigTest {

    @Test
    void retryConfig_ShouldBeAnnotatedWithEnableRetry() {
        RetryConfig config = new RetryConfig();

        assertNotNull(config);

        EnableRetry enableRetryAnnotation = RetryConfig.class.getAnnotation(EnableRetry.class);
        assertNotNull(enableRetryAnnotation, "RetryConfig should be annotated with @EnableRetry");
    }

    @Test
    void retryConfig_ShouldInstantiate() {
        RetryConfig config = new RetryConfig();

        assertNotNull(config);
    }
}
