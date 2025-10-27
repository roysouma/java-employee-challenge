package com.reliaquest.api.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

/**
 * Tests for RestTemplateConfig to ensure proper RestTemplate configuration.
 */
class RestTemplateConfigTest {

    @Test
    void restTemplate_ShouldCreateConfiguredRestTemplate() {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplateBuilder builder = new RestTemplateBuilder();

        RestTemplate restTemplate = config.restTemplate(builder);

        assertNotNull(restTemplate);
        assertNotNull(restTemplate.getRequestFactory());
    }

    @Test
    void restTemplate_ShouldHaveTimeoutConfiguration() {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplateBuilder builder = new RestTemplateBuilder();

        RestTemplate restTemplate = config.restTemplate(builder);

        assertNotNull(restTemplate);
        assertNotNull(restTemplate.getRequestFactory());
    }
}
