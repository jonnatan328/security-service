package com.company.security.password.infrastructure.adapter.output.persistence.repository;

import com.company.security.password.infrastructure.adapter.output.persistence.document.PasswordResetTokenDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PasswordResetTokenRepository extends ReactiveMongoRepository<PasswordResetTokenDocument, String> {

    Mono<PasswordResetTokenDocument> findByToken(String token);

    Flux<PasswordResetTokenDocument> findByUserIdAndStatus(String userId, String status);
}
