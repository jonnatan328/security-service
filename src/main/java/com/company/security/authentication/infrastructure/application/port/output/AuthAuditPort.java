package com.company.security.authentication.infrastructure.application.port.output;

import reactor.core.publisher.Mono;

/**
 * Output port for authentication audit logging.
 * Records authentication events for security and compliance.
 */
public interface AuthAuditPort {

    /**
     * Authentication event types.
     */
    enum EventType {
        SIGN_IN_SUCCESS,
        SIGN_IN_FAILED,
        SIGN_OUT,
        TOKEN_REFRESH,
        TOKEN_REVOKED
    }

    /**
     * Records a successful sign-in event.
     *
     * @param userId    the user ID
     * @param username  the username
     * @param ipAddress the client IP address
     * @param userAgent the client user agent
     * @return a Mono that completes when the event is recorded
     */
    Mono<Void> recordSignInSuccess(String userId, String username, String ipAddress, String userAgent);

    /**
     * Records a failed sign-in event.
     *
     * @param username      the username
     * @param ipAddress     the client IP address
     * @param userAgent     the client user agent
     * @param failureReason the reason for the failure
     * @return a Mono that completes when the event is recorded
     */
    Mono<Void> recordSignInFailure(String username, String ipAddress, String userAgent, String failureReason);

    /**
     * Records a sign-out event.
     *
     * @param userId    the user ID
     * @param username  the username
     * @param ipAddress the client IP address
     * @param userAgent the client user agent
     * @return a Mono that completes when the event is recorded
     */
    Mono<Void> recordSignOut(String userId, String username, String ipAddress, String userAgent);

    /**
     * Records a token refresh event.
     *
     * @param userId    the user ID
     * @param username  the username
     * @param ipAddress the client IP address
     * @param userAgent the client user agent
     * @return a Mono that completes when the event is recorded
     */
    Mono<Void> recordTokenRefresh(String userId, String username, String ipAddress, String userAgent);

    /**
     * Records a token revocation event.
     *
     * @param userId    the user ID
     * @param username  the username
     * @param ipAddress the client IP address
     * @param userAgent the client user agent
     * @return a Mono that completes when the event is recorded
     */
    Mono<Void> recordTokenRevoked(String userId, String username, String ipAddress, String userAgent);
}
