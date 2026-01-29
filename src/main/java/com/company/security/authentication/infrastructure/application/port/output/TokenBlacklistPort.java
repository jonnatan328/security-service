package com.company.security.authentication.infrastructure.application.port.output;

import reactor.core.publisher.Mono;

/**
 * Output port for token blacklist operations.
 * Used to invalidate tokens before their natural expiration.
 */
public interface TokenBlacklistPort {

    /**
     * Adds a token to the blacklist.
     *
     * @param jti               the JWT ID to blacklist
     * @param expirationSeconds the time in seconds until the blacklist entry expires
     * @return a Mono that completes when the token is blacklisted
     */
    Mono<Void> blacklist(String jti, long expirationSeconds);

    /**
     * Checks if a token is blacklisted.
     *
     * @param jti the JWT ID to check
     * @return a Mono containing true if the token is blacklisted
     */
    Mono<Boolean> isBlacklisted(String jti);
}
