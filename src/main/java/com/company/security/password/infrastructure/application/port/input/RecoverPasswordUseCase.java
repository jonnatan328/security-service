package com.company.security.password.infrastructure.application.port.input;

import reactor.core.publisher.Mono;

/**
 * Input port for password recovery use case.
 * Initiates password recovery by generating a reset token.
 */
public interface RecoverPasswordUseCase {

    /**
     * Initiates password recovery for a user.
     * Generates a reset token and publishes an event for notification.
     *
     * @param email     the user's email address
     * @param ipAddress the client IP address for audit purposes
     * @param userAgent the client user agent for audit purposes
     * @return a Mono that completes when the recovery is initiated
     */
    Mono<Void> recoverPassword(String email, String ipAddress, String userAgent);
}
