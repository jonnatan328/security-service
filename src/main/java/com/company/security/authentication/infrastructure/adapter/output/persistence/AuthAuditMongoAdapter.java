package com.company.security.authentication.infrastructure.adapter.output.persistence;

import com.company.security.authentication.infrastructure.adapter.output.persistence.document.AuthAuditDocument;
import com.company.security.authentication.infrastructure.adapter.output.persistence.repository.AuthAuditRepository;
import com.company.security.authentication.domain.port.output.AuthAuditPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

/**
 * MongoDB adapter for authentication audit logging.
 */
public class AuthAuditMongoAdapter implements AuthAuditPort {

    private static final Logger log = LoggerFactory.getLogger(AuthAuditMongoAdapter.class);
    private static final String CORRELATION_ID_KEY = "correlationId";

    private final AuthAuditRepository repository;

    public AuthAuditMongoAdapter(AuthAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Void> recordSignInSuccess(String userId, String username, String ipAddress, String userAgent) {
        return recordEvent(EventType.SIGN_IN_SUCCESS, userId, username, true, null, ipAddress, userAgent);
    }

    @Override
    public Mono<Void> recordSignInFailure(String username, String ipAddress, String userAgent, String failureReason) {
        return recordEvent(EventType.SIGN_IN_FAILED, null, username, false, failureReason, ipAddress, userAgent);
    }

    @Override
    public Mono<Void> recordSignOut(String userId, String username, String ipAddress, String userAgent) {
        return recordEvent(EventType.SIGN_OUT, userId, username, true, null, ipAddress, userAgent);
    }

    @Override
    public Mono<Void> recordTokenRefresh(String userId, String username, String ipAddress, String userAgent) {
        return recordEvent(EventType.TOKEN_REFRESH, userId, username, true, null, ipAddress, userAgent);
    }

    @Override
    public Mono<Void> recordTokenRevoked(String userId, String username, String ipAddress, String userAgent) {
        return recordEvent(EventType.TOKEN_REVOKED, userId, username, true, null, ipAddress, userAgent);
    }

    private Mono<Void> recordEvent(
            EventType eventType,
            String userId,
            String username,
            boolean success,
            String failureReason,
            String ipAddress,
            String userAgent) {

        String correlationId = MDC.get(CORRELATION_ID_KEY);

        AuthAuditDocument.RequestMetadata metadata = new AuthAuditDocument.RequestMetadata(
                ipAddress,
                userAgent,
                correlationId
        );

        AuthAuditDocument document = new AuthAuditDocument(
                eventType.name(),
                userId,
                username,
                success,
                failureReason,
                metadata
        );

        return repository.save(document)
                .doOnSuccess(saved -> log.debug("Audit event recorded: {} for user: {}", eventType, username))
                .doOnError(e -> log.error("Failed to record audit event: {} for user: {}", eventType, username, e))
                .then();
    }
}
