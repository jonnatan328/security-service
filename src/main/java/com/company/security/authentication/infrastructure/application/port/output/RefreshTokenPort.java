package com.company.security.authentication.infrastructure.application.port.output;

import com.company.security.authentication.domain.model.TokenClaims;
import reactor.core.publisher.Mono;

/**
 * Output port for refresh token storage operations.
 * Manages refresh token persistence and lookup.
 */
public interface RefreshTokenPort {

    /**
     * Stores a refresh token.
     *
     * @param userId            the user ID
     * @param deviceId          the device identifier
     * @param claims            the token claims to store
     * @param expirationSeconds the time in seconds until the entry expires
     * @return a Mono that completes when the token is stored
     */
    Mono<Void> store(String userId, String deviceId, TokenClaims claims, long expirationSeconds);

    /**
     * Retrieves stored refresh token claims.
     *
     * @param userId   the user ID
     * @param deviceId the device identifier
     * @return a Mono containing the stored claims if found
     */
    Mono<TokenClaims> retrieve(String userId, String deviceId);

    /**
     * Deletes a stored refresh token.
     *
     * @param userId   the user ID
     * @param deviceId the device identifier
     * @return a Mono that completes when the token is deleted
     */
    Mono<Void> delete(String userId, String deviceId);

    /**
     * Deletes all refresh tokens for a user.
     *
     * @param userId the user ID
     * @return a Mono that completes when all tokens are deleted
     */
    Mono<Void> deleteAllForUser(String userId);
}
