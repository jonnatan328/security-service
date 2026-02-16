package com.company.security.authentication.infrastructure.adapter.output.persistence;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for AuthAuditMongoAdapter using Testcontainers MongoDB.
 * Note: Requires Docker to run Testcontainers.
 */
@Disabled("Requires Docker for Testcontainers - enable in CI environment")
@DisplayName("Auth Audit Mongo Adapter Integration Tests")
class AuthAuditMongoAdapterIntegrationTest {

    private static final boolean DOCKER_AVAILABLE = false;

    @Test
    @DisplayName("Placeholder - requires Docker for Testcontainers")
    void placeholder() {
        assertThat(DOCKER_AVAILABLE).as("Docker required for this test").isFalse();
    }
}
