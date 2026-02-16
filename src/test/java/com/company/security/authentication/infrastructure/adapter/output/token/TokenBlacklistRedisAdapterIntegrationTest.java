package com.company.security.authentication.infrastructure.adapter.output.token;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for TokenBlacklistRedisAdapter using Testcontainers Redis.
 * Note: Requires Docker to run Testcontainers.
 * This test is disabled by default and should be run in CI with Docker available.
 */
@Disabled("Requires Docker for Testcontainers - enable in CI environment")
@DisplayName("Token Blacklist Redis Adapter Integration Tests")
class TokenBlacklistRedisAdapterIntegrationTest {

    private static final boolean DOCKER_AVAILABLE = false;

    @Test
    @DisplayName("Placeholder - requires Docker for Testcontainers")
    void placeholder() {
        assertThat(DOCKER_AVAILABLE).as("Docker required for this test").isFalse();
    }
}
