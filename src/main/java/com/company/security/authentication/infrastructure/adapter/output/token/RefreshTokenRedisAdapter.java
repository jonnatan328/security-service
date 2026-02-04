package com.company.security.authentication.infrastructure.adapter.output.token;

import com.company.security.authentication.domain.model.TokenClaims;
import com.company.security.authentication.domain.port.output.RefreshTokenPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis adapter for refresh token storage.
 * Stores refresh token claims with TTL for session management.
 */
public class RefreshTokenRedisAdapter implements RefreshTokenPort {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenRedisAdapter.class);
    private static final String KEY_PREFIX = "security:refresh:";
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RefreshTokenRedisAdapter(ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> store(String userId, String deviceId, TokenClaims claims, long expirationSeconds) {
        String key = buildKey(userId, deviceId);
        Duration ttl = Duration.ofSeconds(Math.max(1, expirationSeconds));

        return Mono.fromCallable(() -> serializeClaims(claims))
                .flatMap(serialized -> redisTemplate.opsForValue()
                        .set(key, serialized, ttl))
                .doOnSuccess(success -> log.debug("Refresh token stored for user: {}, device: {}", userId, deviceId))
                .doOnError(e -> log.error("Failed to store refresh token for user: {}", userId, e))
                .then();
    }

    @Override
    public Mono<TokenClaims> retrieve(String userId, String deviceId) {
        String key = buildKey(userId, deviceId);

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(serialized -> Mono.fromCallable(() -> deserializeClaims(serialized)))
                .doOnNext(claims -> log.debug("Refresh token retrieved for user: {}, device: {}", userId, deviceId))
                .doOnError(e -> log.error("Failed to retrieve refresh token for user: {}", userId, e));
    }

    @Override
    public Mono<Void> delete(String userId, String deviceId) {
        String key = buildKey(userId, deviceId);

        return redisTemplate.delete(key)
                .doOnSuccess(count -> log.debug("Refresh token deleted for user: {}, device: {}", userId, deviceId))
                .doOnError(e -> log.error("Failed to delete refresh token for user: {}", userId, e))
                .then();
    }

    @Override
    public Mono<Void> deleteAllForUser(String userId) {
        String pattern = KEY_PREFIX + userId + ":*";

        return redisTemplate.scan(ScanOptions.scanOptions().match(pattern).build())
                .flatMap(key -> redisTemplate.delete(key))
                .then()
                .doOnSuccess(v -> log.debug("All refresh tokens deleted for user: {}", userId))
                .doOnError(e -> log.error("Failed to delete all refresh tokens for user: {}", userId, e));
    }

    private String buildKey(String userId, String deviceId) {
        return KEY_PREFIX + userId + ":" + deviceId;
    }

    private String serializeClaims(TokenClaims claims) {
        try {
            Map<String, Object> map = Map.ofEntries(
                    Map.entry("jti", claims.jti()),
                    Map.entry("subject", claims.subject()),
                    Map.entry("userId", claims.userId()),
                    Map.entry("username", claims.username()),
                    Map.entry("email", claims.email()),
                    Map.entry("roles", claims.roles()),
                    Map.entry("deviceId", claims.deviceId()),
                    Map.entry("issuedAt", claims.issuedAt().toEpochMilli()),
                    Map.entry("expiresAt", claims.expiresAt().toEpochMilli()),
                    Map.entry("issuer", claims.issuer()));
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("Failed to serialize token claims", e);
        }
    }

    @SuppressWarnings("unchecked")
    private TokenClaims deserializeClaims(String serialized) {
        try {
            Map<String, Object> map = objectMapper.readValue(serialized, MAP_TYPE_REF);

            List<String> rolesList = (List<String>) map.get("roles");
            Set<String> roles = rolesList != null ? new HashSet<>(rolesList) : Collections.emptySet();

            return TokenClaims.builder()
                    .jti((String) map.get("jti"))
                    .subject((String) map.get("subject"))
                    .userId((String) map.get("userId"))
                    .username((String) map.get("username"))
                    .email((String) map.get("email"))
                    .roles(roles)
                    .deviceId((String) map.get("deviceId"))
                    .issuedAt(Instant.ofEpochMilli(((Number) map.get("issuedAt")).longValue()))
                    .expiresAt(Instant.ofEpochMilli(((Number) map.get("expiresAt")).longValue()))
                    .issuer((String) map.get("issuer"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("Failed to deserialize token claims", e);
        }
    }
}
