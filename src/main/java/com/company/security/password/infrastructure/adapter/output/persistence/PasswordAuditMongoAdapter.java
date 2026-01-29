package com.company.security.password.infrastructure.adapter.output.persistence;

import com.company.security.password.infrastructure.adapter.output.persistence.document.PasswordAuditDocument;
import com.company.security.password.infrastructure.adapter.output.persistence.repository.PasswordAuditRepository;
import com.company.security.password.infrastructure.application.port.output.PasswordAuditPort;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PasswordAuditMongoAdapter implements PasswordAuditPort {

    private final PasswordAuditRepository repository;

    public PasswordAuditMongoAdapter(PasswordAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Void> recordEvent(EventType eventType, String userId, String email, boolean success,
                                   String failureReason, String ipAddress, String userAgent) {
        String correlationId = MDC.get("correlationId");
        PasswordAuditDocument.RequestMetadata metadata =
                new PasswordAuditDocument.RequestMetadata(ipAddress, userAgent, correlationId);

        PasswordAuditDocument document = new PasswordAuditDocument(
                eventType.name(), userId, email, success, failureReason, metadata);

        return repository.save(document).then();
    }
}
