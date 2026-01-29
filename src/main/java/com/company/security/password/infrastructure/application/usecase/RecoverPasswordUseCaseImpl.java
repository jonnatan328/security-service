package com.company.security.password.infrastructure.application.usecase;

import com.company.security.password.domain.model.PasswordResetToken;
import com.company.security.password.infrastructure.application.port.input.RecoverPasswordUseCase;
import com.company.security.password.infrastructure.application.port.output.*;
import com.company.security.shared.domain.model.Email;
import com.company.security.shared.infrastructure.properties.PasswordPolicyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the password recovery use case.
 */
@Service
public class RecoverPasswordUseCaseImpl implements RecoverPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecoverPasswordUseCaseImpl.class);

    private final UserLookupPort userLookupPort;
    private final PasswordResetTokenPort passwordResetTokenPort;
    private final EventPublisherPort eventPublisherPort;
    private final PasswordAuditPort passwordAuditPort;
    private final PasswordPolicyProperties passwordPolicyProperties;

    public RecoverPasswordUseCaseImpl(
            UserLookupPort userLookupPort,
            PasswordResetTokenPort passwordResetTokenPort,
            EventPublisherPort eventPublisherPort,
            PasswordAuditPort passwordAuditPort,
            PasswordPolicyProperties passwordPolicyProperties) {
        this.userLookupPort = userLookupPort;
        this.passwordResetTokenPort = passwordResetTokenPort;
        this.eventPublisherPort = eventPublisherPort;
        this.passwordAuditPort = passwordAuditPort;
        this.passwordPolicyProperties = passwordPolicyProperties;
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
                                        passwordPolicyProperties.getResetTokenExpiration()
                                );

                                return passwordResetTokenPort.save(resetToken)
                                        .flatMap(saved -> {
                                            String resetUrl = buildResetUrl(saved.token());
                                            return eventPublisherPort.publishPasswordResetRequested(saved, resetUrl);
                                        })
                                        .then(passwordAuditPort.recordEvent(
                                                PasswordAuditPort.EventType.PASSWORD_RESET_REQUESTED,
                                                userInfo.userId(),
                                                email,
                                                true,
                                                null,
                                                ipAddress,
                                                userAgent));
                            }));
                })
                .onErrorResume(e -> {
                    log.warn("Password recovery failed for email: {} - {}", email, e.getMessage());
                    return Mono.empty();
                })
                .then()
                .doOnSuccess(v -> log.info("Password recovery processed for email: {}", email));
    }

    private String buildResetUrl(String token) {
        return passwordPolicyProperties.getResetBaseUrl() + "?token=" + token;
    }
}
