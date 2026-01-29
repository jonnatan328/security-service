package com.company.security.authentication.infrastructure.adapter.output.persistence.repository;

import com.company.security.authentication.infrastructure.adapter.output.persistence.document.AuthAuditDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.Instant;

/**
 * Reactive MongoDB repository for authentication audit logs.
 */
@Repository
public interface AuthAuditRepository extends ReactiveMongoRepository<AuthAuditDocument, String> {

    Flux<AuthAuditDocument> findByUsernameOrderByTimestampDesc(String username);

    Flux<AuthAuditDocument> findByUserIdOrderByTimestampDesc(String userId);

    Flux<AuthAuditDocument> findByEventTypeAndTimestampAfter(String eventType, Instant after);

    Flux<AuthAuditDocument> findByUsernameAndEventTypeAndTimestampAfter(
            String username, String eventType, Instant after);
}
