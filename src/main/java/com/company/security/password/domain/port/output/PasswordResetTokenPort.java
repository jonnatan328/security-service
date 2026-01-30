package com.company.security.password.domain.port.output;

import com.company.security.password.domain.model.PasswordResetToken;
import reactor.core.publisher.Mono;

/**
 * Output port for password reset token persistence operations.
 */
public interface PasswordResetTokenPort {

    Mono<PasswordResetToken> save(PasswordResetToken token);

    Mono<PasswordResetToken> findByToken(String token);

    Mono<Void> cancelAllPendingTokensForUser(String userId);

    Mono<PasswordResetToken> markAsUsed(String token);
}
