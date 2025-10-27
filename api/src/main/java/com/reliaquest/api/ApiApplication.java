package com.reliaquest.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class ApiApplication {

    private final Environment environment;

    public static void main(String[] args) {
        log.info("Starting Employee API application...");
        SpringApplication.run(ApiApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = environment.getProperty("server.port", "8111");
        String baseUrl = environment.getProperty("employee.api.base-url", "http://localhost:8112/api/v1/employee");
        String profile = String.join(",", environment.getActiveProfiles());

        log.info("=== Employee API Started Successfully ===");
        log.info("Server running on port: {}", port);
        log.info("External API URL: {}", baseUrl);
        log.info("Active profiles: {}", profile.isEmpty() ? "default" : profile);
        log.info("Application ready to accept requests at: http://localhost:{}/api/v1/employee", port);
        log.info("========================================");
    }
}
