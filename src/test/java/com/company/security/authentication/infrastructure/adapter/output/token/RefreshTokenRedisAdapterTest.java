package com.company.security.authentication.infrastructure.adapter.output.token;

import com.company.security.authentication.domain.model.TokenClaims;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRedisAdapterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RefreshTokenRedisAdapter adapter;

    private static final String USER_ID = "user-123";
    private static final String DEVICE_ID = "device-001";

    @BeforeEach
    void setUp() {
        adapter = new RefreshTokenRedisAdapter(redisTemplate, objectMapper);
    }

    @Test
    void store_withValidClaims_storesInRedis() {
        TokenClaims claims = buildTokenClaims();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.set(eq("security:refresh:user-123:device-001"), anyString(), any(Duration.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(adapter.store(USER_ID, DEVICE_ID, claims, 86400))
                .verifyComplete();
    }

    @Test
    void retrieve_withExistingToken_returnsTokenClaims() {
        TokenClaims original = buildTokenClaims();

        // Serialize manually to provide test data
        String serialized = serializeClaims(original);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("security:refresh:user-123:device-001")).thenReturn(Mono.just(serialized));

        StepVerifier.create(adapter.retrieve(USER_ID, DEVICE_ID))
                .assertNext(claims -> {
                    assertThat(claims.jti()).isEqualTo(original.jti());
                    assertThat(claims.userId()).isEqualTo(USER_ID);
                    assertThat(claims.username()).isEqualTo("john.doe");
                    assertThat(claims.email()).isEqualTo("john@company.com");
                    assertThat(claims.roles()).contains("ROLE_USER");
                })
                .verifyComplete();
    }

    @Test
    void retrieve_withNonExistingToken_returnsEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("security:refresh:user-123:device-001")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.retrieve(USER_ID, DEVICE_ID))
                .verifyComplete();
    }

    @Test
    void delete_deletesFromRedis() {
        when(redisTemplate.delete("security:refresh:user-123:device-001")).thenReturn(Mono.just(1L));

        StepVerifier.create(adapter.delete(USER_ID, DEVICE_ID))
                .verifyComplete();
    }

    @Test
    void deleteAllForUser_deletesAllKeysForUser() {
        when(redisTemplate.scan(any())).thenReturn(Flux.just("security:refresh:user-123:device-001"));
        when(redisTemplate.delete(anyString())).thenReturn(Mono.just(1L));

        StepVerifier.create(adapter.deleteAllForUser(USER_ID))
                .verifyComplete();
    }

    private TokenClaims buildTokenClaims() {
        Instant now = Instant.now();
        return TokenClaims.builder()
                .jti("jti-123")
                .subject(USER_ID)
                .userId(USER_ID)
                .username("john.doe")
                .email("john@company.com")
                .roles(Set.of("ROLE_USER"))
                .deviceId(DEVICE_ID)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(86400))
                .issuer("security-service")
                .build();
    }

    private String serializeClaims(TokenClaims claims) {
        try {
            return objectMapper.writeValueAsString(java.util.Map.ofEntries(
                    java.util.Map.entry("jti", claims.jti()),
                    java.util.Map.entry("subject", claims.subject()),
                    java.util.Map.entry("userId", claims.userId()),
                    java.util.Map.entry("username", claims.username()),
                    java.util.Map.entry("email", claims.email()),
                    java.util.Map.entry("roles", claims.roles()),
                    java.util.Map.entry("deviceId", claims.deviceId()),
                    java.util.Map.entry("issuedAt", claims.issuedAt().toEpochMilli()),
                    java.util.Map.entry("expiresAt", claims.expiresAt().toEpochMilli()),
                    java.util.Map.entry("issuer", claims.issuer())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
