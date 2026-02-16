package com.company.security.shared.infrastructure.adapter.output.ratelimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisRateLimitAspectTest {

    @Mock
    private RateLimitingRedisAdapter rateLimitingAdapter;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private RedisRateLimited annotation;

    private RedisRateLimitAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new RedisRateLimitAspect(rateLimitingAdapter);
    }

    @Test
    void around_withAllowedRequest_proceedsWithMethod() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"request", "ipAddress"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"some-request", "192.168.1.1"});
        when(annotation.keyPrefix()).thenReturn("security:ratelimit:test:");
        when(annotation.maxRequests()).thenReturn(10);
        when(annotation.windowSeconds()).thenReturn(60L);
        when(annotation.keyParamName()).thenReturn("ipAddress");

        when(rateLimitingAdapter.checkRateLimit(
                "security:ratelimit:test:192.168.1.1", 10, Duration.ofSeconds(60)))
                .thenReturn(Mono.empty());
        when(joinPoint.proceed()).thenReturn(Mono.just("result"));

        Object result = aspect.around(joinPoint, annotation);

        @SuppressWarnings("unchecked")
        Mono<Object> mono = (Mono<Object>) result;
        StepVerifier.create(mono)
                .expectNext("result")
                .verifyComplete();
    }

    @Test
    void around_withMissingParameter_throwsIllegalArgumentException() {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"request"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{"some-request"});
        when(annotation.keyParamName()).thenReturn("ipAddress");

        assertThatThrownBy(() -> aspect.around(joinPoint, annotation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ipAddress");
    }
}
