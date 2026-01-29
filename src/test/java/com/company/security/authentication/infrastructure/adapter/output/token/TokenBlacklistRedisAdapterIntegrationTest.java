package com.company.security.authentication.infrastructure.adapter.output.token;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration test for TokenBlacklistRedisAdapter using Testcontainers Redis.
 * Note: Requires Docker to run Testcontainers.
 * This test is disabled by default and should be run in CI with Docker available.
 */
@Disabled("Requires Docker for Testcontainers - enable in CI environment")
@DisplayName("Token Blacklist Redis Adapter Integration Tests")
class TokenBlacklistRedisAdapterIntegrationTest {

    // To enable this test:
    // 1. Ensure Docker is running
    // 2. Remove @Disabled annotation
    // 3. Uncomment the test configuration below

    // @Container
    // static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
    //         .withExposedPorts(6379);

    // @DynamicPropertySource
    // static void redisProperties(DynamicPropertyRegistry registry) {
    //     registry.add("spring.data.redis.host", redis::getHost);
    //     registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    // }

    // @Autowired
    // private TokenBlacklistRedisAdapter adapter;

    @Test
    @DisplayName("Placeholder - requires Docker for Testcontainers")
    void placeholder() {
        // This test requires Docker with Testcontainers
        // Enable when running in CI environment with Docker
    }
}
