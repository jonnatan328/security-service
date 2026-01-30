package com.company.security.authentication.domain.port.output;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.TokenClaims;
import com.company.security.authentication.domain.model.TokenPair;
import reactor.core.publisher.Mono;

/**
 * Output port for token generation and parsing operations.
 */
public interface TokenProviderPort {

    /**
     * Generates a new token pair for the authenticated user.
     *
     * @param user     the authenticated user
     * @param deviceId the device identifier for the refresh token
     * @return a Mono containing the generated token pair
     */
    Mono<TokenPair> generateTokenPair(AuthenticatedUser user, String deviceId);

    /**
     * Parses and validates an access token.
     *
     * @param accessToken the access token to parse
     * @return a Mono containing the token claims
     */
    Mono<TokenClaims> parseAccessToken(String accessToken);

    /**
     * Parses and validates a refresh token.
     *
     * @param refreshToken the refresh token to parse
     * @return a Mono containing the token claims
     */
    Mono<TokenClaims> parseRefreshToken(String refreshToken);

    /**
     * Extracts the JTI (JWT ID) from a token without full validation.
     *
     * @param token the token
     * @return the JTI or null if not extractable
     */
    String extractJti(String token);
}
