package com.company.security.password.infrastructure.adapter.output.persistence;

import com.company.security.password.domain.model.PasswordResetToken;
import com.company.security.password.infrastructure.adapter.output.persistence.document.PasswordResetTokenDocument;
import com.company.security.password.infrastructure.adapter.output.persistence.repository.PasswordResetTokenRepository;
import com.company.security.password.domain.port.output.PasswordResetTokenPort;
import com.company.security.shared.domain.model.Email;
import reactor.core.publisher.Mono;

import java.time.Instant;

public class PasswordResetTokenMongoAdapter implements PasswordResetTokenPort {

    private final PasswordResetTokenRepository repository;

    public PasswordResetTokenMongoAdapter(PasswordResetTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PasswordResetToken> save(PasswordResetToken token) {
        PasswordResetTokenDocument doc = toDocument(token);
        return repository.save(doc).map(this::toDomain);
    }

    @Override
    public Mono<PasswordResetToken> findByToken(String token) {
        return repository.findByToken(token).map(this::toDomain);
    }

    @Override
    public Mono<Void> cancelAllPendingTokensForUser(String userId) {
        return repository.findByUserIdAndStatus(userId, "PENDING")
                .flatMap(doc -> {
                    doc.setStatus("CANCELLED");
                    return repository.save(doc);
                })
                .then();
    }

    @Override
    public Mono<PasswordResetToken> markAsUsed(String token) {
        return repository.findByToken(token)
                .flatMap(doc -> {
                    doc.setStatus("USED");
                    doc.setUsedAt(Instant.now());
                    return repository.save(doc);
                })
                .map(this::toDomain);
    }

    private PasswordResetTokenDocument toDocument(PasswordResetToken token) {
        PasswordResetTokenDocument doc = new PasswordResetTokenDocument();
        doc.setId(token.id());
        doc.setToken(token.token());
        doc.setUserId(token.userId());
        doc.setEmail(token.email().value());
        doc.setCreatedAt(token.createdAt());
        doc.setExpiresAt(token.expiresAt());
        doc.setUsedAt(token.usedAt());
        doc.setStatus(token.status().name());
        return doc;
    }

    private PasswordResetToken toDomain(PasswordResetTokenDocument doc) {
        return PasswordResetToken.builder()
                .id(doc.getId())
                .token(doc.getToken())
                .userId(doc.getUserId())
                .email(Email.of(doc.getEmail()))
                .createdAt(doc.getCreatedAt())
                .expiresAt(doc.getExpiresAt())
                .usedAt(doc.getUsedAt())
                .status(PasswordResetToken.Status.valueOf(doc.getStatus()))
                .build();
    }
}
