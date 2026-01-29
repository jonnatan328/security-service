package com.company.security.authentication.infrastructure.application.port.input;

import com.company.security.authentication.domain.model.AuthenticationResult;
import com.company.security.authentication.domain.model.Credentials;
import reactor.core.publisher.Mono;

/**
 * Input port for sign-in use case.
 * Handles user authentication against the directory service.
 */
public interface SignInUseCase {

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param credentials the user credentials
     * @param ipAddress   the client IP address for audit purposes
     * @param userAgent   the client user agent for audit purposes
     * @return a Mono containing the authentication result with tokens
     */
    Mono<AuthenticationResult> signIn(Credentials credentials, String ipAddress, String userAgent);
}
