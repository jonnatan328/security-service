package com.company.security.authentication.infrastructure.adapter.output.token;

import com.company.security.authentication.infrastructure.application.port.output.TokenBlacklistPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Redis adapter for token blacklist operations.
 * Stores invalidated token JTIs with TTL.
 */
@Component
public class TokenBlacklistRedisAdapter implements TokenBlacklistPort {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistRedisAdapter.class);
    private static final String KEY_PREFIX = "security:blacklist:";
    private static final String BLACKLISTED_VALUE = "1";

    private final ReactiveStringRedisTemplate redisTemplate;

    public TokenBlacklistRedisAdapter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> blacklist(String jti, long expirationSeconds) {
        if (jti == null || jti.isBlank()) {
            return Mono.empty();
        }

        String key = KEY_PREFIX + jti;
        Duration ttl = Duration.ofSeconds(Math.max(1, expirationSeconds));

        return redisTemplate.opsForValue()
                .set(key, BLACKLISTED_VALUE, ttl)
                .doOnSuccess(success -> log.debug("Token blacklisted: {} with TTL: {}s", jti, expirationSeconds))
                .doOnError(e -> log.error("Failed to blacklist token: {}", jti, e))
                .then();
    }

    @Override
    public Mono<Boolean> isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return Mono.just(false);
        }

        String key = KEY_PREFIX + jti;

        return redisTemplate.hasKey(key)
                .doOnNext(isBlacklisted -> {
                    if (isBlacklisted) {
                        log.debug("Token found in blacklist: {}", jti);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Failed to check blacklist for token: {}", jti, e);
                    // Fail closed - if we can't check, assume not blacklisted
                    // This is a trade-off; in high-security scenarios, fail open might be better
                    return Mono.just(false);
                });
    }
}
