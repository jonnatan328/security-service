package com.company.security.authentication.infrastructure.adapter.output.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistRedisAdapterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    private TokenBlacklistRedisAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TokenBlacklistRedisAdapter(redisTemplate);
    }

    @Test
    void blacklist_withValidJti_storesInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.set(eq("security:blacklist:jti-123"), eq("1"), any(Duration.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(adapter.blacklist("jti-123", 300))
                .verifyComplete();
    }

    @Test
    void blacklist_withNullJti_completesEmpty() {
        StepVerifier.create(adapter.blacklist(null, 300))
                .verifyComplete();
    }

    @Test
    void blacklist_withBlankJti_completesEmpty() {
        StepVerifier.create(adapter.blacklist("  ", 300))
                .verifyComplete();
    }

    @Test
    void blacklist_withNegativeExpiration_usesMinimumTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.set("security:blacklist:jti-123", "1", Duration.ofSeconds(1)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(adapter.blacklist("jti-123", -10))
                .verifyComplete();
    }

    @Test
    void isBlacklisted_withBlacklistedJti_returnsTrue() {
        when(redisTemplate.hasKey("security:blacklist:jti-123")).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.isBlacklisted("jti-123"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isBlacklisted_withNonBlacklistedJti_returnsFalse() {
        when(redisTemplate.hasKey("security:blacklist:jti-456")).thenReturn(Mono.just(false));

        StepVerifier.create(adapter.isBlacklisted("jti-456"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isBlacklisted_withNullJti_returnsFalse() {
        StepVerifier.create(adapter.isBlacklisted(null))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isBlacklisted_withBlankJti_returnsFalse() {
        StepVerifier.create(adapter.isBlacklisted(""))
                .expectNext(false)
                .verifyComplete();
    }
}
