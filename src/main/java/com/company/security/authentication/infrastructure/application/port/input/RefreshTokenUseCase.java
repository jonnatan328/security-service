package com.company.security.authentication.infrastructure.application.port.input;

import com.company.security.authentication.domain.model.TokenPair;
import reactor.core.publisher.Mono;

/**
 * Input port for refresh token use case.
 * Handles token refresh operations.
 */
public interface RefreshTokenUseCase {

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @param ipAddress    the client IP address for audit purposes
     * @param userAgent    the client user agent for audit purposes
     * @return a Mono containing the new token pair
     */
    Mono<TokenPair> refreshToken(String refreshToken, String ipAddress, String userAgent);
}
