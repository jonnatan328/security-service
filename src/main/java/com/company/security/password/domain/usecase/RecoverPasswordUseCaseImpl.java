package com.company.security.password.domain.usecase;

import com.company.security.password.domain.model.PasswordRecoverySettings;
import com.company.security.password.domain.model.PasswordResetToken;
import com.company.security.password.domain.port.input.RecoverPasswordUseCase;
import com.company.security.password.domain.port.output.*;
import com.company.security.shared.domain.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Implementation of the password recovery use case.
 */
public class RecoverPasswordUseCaseImpl implements RecoverPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecoverPasswordUseCaseImpl.class);

    private final UserLookupPort userLookupPort;
    private final PasswordResetTokenPort passwordResetTokenPort;
    private final EventPublisherPort eventPublisherPort;
    private final PasswordAuditPort passwordAuditPort;
    private final PasswordRecoverySettings passwordRecoverySettings;

    public RecoverPasswordUseCaseImpl(
            UserLookupPort userLookupPort,
            PasswordResetTokenPort passwordResetTokenPort,
            EventPublisherPort eventPublisherPort,
            PasswordAuditPort passwordAuditPort,
            PasswordRecoverySettings passwordRecoverySettings) {
        this.userLookupPort = userLookupPort;
        this.passwordResetTokenPort = passwordResetTokenPort;
        this.eventPublisherPort = eventPublisherPort;
        this.passwordAuditPort = passwordAuditPort;
        this.passwordRecoverySettings = passwordRecoverySettings;
    }

    @Override
    public Mono<Void> recoverPassword(String email, String ipAddress, String userAgent) {
        log.debug("Processing password recovery for email: {}", email);

        // Always return success to prevent email enumeration
        return userLookupPort.findByEmail(email)
                .flatMap(userInfo -> {
                    Email emailVO = Email.of(userInfo.email());

                    // Cancel existing pending tokens
                    return passwordResetTokenPort.cancelAllPendingTokensForUser(userInfo.userId())
                            .then(Mono.defer(() -> {
                                // Create new reset token
                                PasswordResetToken resetToken = PasswordResetToken.create(
                                        userInfo.userId(),
                                        emailVO,
                                        passwordRecoverySettings.resetTokenExpirationSeconds()
                                );

                                return passwordResetTokenPort.save(resetToken)
                                        .flatMap(saved -> {
                                            String resetUrl = buildResetUrl(saved.token());
                                            return eventPublisherPort.publishPasswordResetRequested(saved, resetUrl);
                                        })
                                        .doOnSuccess(v -> recordAudit(
                                                userInfo.userId(), email, ipAddress, userAgent));
                            }));
                })
                .onErrorResume(e -> {
                    log.warn("Password recovery failed for email: {} - {}", email, e.getMessage());
                    return Mono.empty();
                })
                .then()
                .doOnSuccess(v -> log.info("Password recovery processed for email: {}", email));
    }

    private void recordAudit(String userId, String email, String ipAddress, String userAgent) {
        passwordAuditPort.recordEvent(
                        PasswordAuditPort.EventType.PASSWORD_RESET_REQUESTED,
                        userId, email, true, null, ipAddress, userAgent)
                .subscribe(
                        null,
                        error -> log.warn("Failed to record password recovery audit for email: {}", email, error)
                );
    }

    private String buildResetUrl(String token) {
        return passwordRecoverySettings.resetBaseUrl() + "?token=" + token;
    }
}
