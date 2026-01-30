package com.company.security.authentication.domain.port.input;

import reactor.core.publisher.Mono;

/**
 * Input port for sign-out use case.
 * Handles user sign-out by invalidating tokens.
 */
public interface SignOutUseCase {

    /**
     * Signs out a user by invalidating their tokens.
     *
     * @param accessToken  the access token to invalidate
     * @param refreshToken the refresh token to invalidate (optional)
     * @param ipAddress    the client IP address for audit purposes
     * @param userAgent    the client user agent for audit purposes
     * @return a Mono that completes when sign-out is successful
     */
    Mono<Void> signOut(String accessToken, String refreshToken, String ipAddress, String userAgent);
}
