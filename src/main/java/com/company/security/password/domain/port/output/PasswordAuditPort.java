package com.company.security.password.domain.port.output;

import reactor.core.publisher.Mono;

/**
 * Output port for password audit logging.
 */
public interface PasswordAuditPort {

    enum EventType {
        PASSWORD_RESET_REQUESTED,
        PASSWORD_RESET_COMPLETED,
        PASSWORD_UPDATED,
        PASSWORD_RESET_FAILED
    }

    Mono<Void> recordEvent(EventType eventType, String userId, String email, boolean success,
                           String failureReason, String ipAddress, String userAgent);
}
