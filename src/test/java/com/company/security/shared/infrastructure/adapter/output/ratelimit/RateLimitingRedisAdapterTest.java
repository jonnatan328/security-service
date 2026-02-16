package com.company.security.shared.infrastructure.adapter.output.ratelimit;

import com.company.security.shared.infrastructure.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingRedisAdapterTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    private RateLimitingRedisAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RateLimitingRedisAdapter(redisTemplate);
    }

    @Test
    void checkRateLimit_firstRequest_setsExpireAndAllows() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("test-key")).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(eq("test-key"), any(Duration.class))).thenReturn(Mono.just(true));

        StepVerifier.create(adapter.checkRateLimit("test-key", 10, Duration.ofSeconds(60)))
                .verifyComplete();
    }

    @Test
    void checkRateLimit_underLimit_allows() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("test-key")).thenReturn(Mono.just(5L));

        StepVerifier.create(adapter.checkRateLimit("test-key", 10, Duration.ofSeconds(60)))
                .verifyComplete();
    }

    @Test
    void checkRateLimit_overLimit_throwsRateLimitExceededException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("test-key")).thenReturn(Mono.just(11L));

        StepVerifier.create(adapter.checkRateLimit("test-key", 10, Duration.ofSeconds(60)))
                .expectError(RateLimitExceededException.class)
                .verify();
    }

    @Test
    void checkRateLimit_atExactLimit_allows() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("test-key")).thenReturn(Mono.just(10L));

        StepVerifier.create(adapter.checkRateLimit("test-key", 10, Duration.ofSeconds(60)))
                .verifyComplete();
    }
}
