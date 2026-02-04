package com.company.security.shared.infrastructure.adapter.output.ratelimit;

import com.company.security.shared.infrastructure.exception.RateLimitExceededException;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class RateLimitingRedisAdapter {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RateLimitingRedisAdapter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Void> checkRateLimit(String key, int maxRequests, Duration window) {
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1L) {
                        return redisTemplate.expire(key, window).thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > maxRequests) {
                        return Mono.error(new RateLimitExceededException(
                                "Rate limit exceeded. Try again later."));
                    }
                    return Mono.empty();
                });
    }
}
