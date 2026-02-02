package com.company.security.password.domain.usecase;

import com.company.security.password.domain.exception.PasswordResetTokenExpiredException;
import com.company.security.password.domain.exception.PasswordResetTokenInvalidException;
import com.company.security.password.domain.model.PasswordChangeResult;
import com.company.security.password.domain.service.PasswordPolicyService;
import com.company.security.password.domain.port.input.ResetPasswordUseCase;
import com.company.security.password.domain.port.output.DirectoryPasswordPort;
import com.company.security.password.domain.port.output.PasswordAuditPort;
import com.company.security.password.domain.port.output.PasswordResetTokenPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Implementation of the password reset use case.
 */
public class ResetPasswordUseCaseImpl implements ResetPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordUseCaseImpl.class);

    private final PasswordResetTokenPort passwordResetTokenPort;
    private final DirectoryPasswordPort directoryPasswordPort;
    private final PasswordAuditPort passwordAuditPort;
    private final PasswordPolicyService passwordPolicyService;

    public ResetPasswordUseCaseImpl(
            PasswordResetTokenPort passwordResetTokenPort,
            DirectoryPasswordPort directoryPasswordPort,
            PasswordAuditPort passwordAuditPort,
            PasswordPolicyService passwordPolicyService) {
        this.passwordResetTokenPort = passwordResetTokenPort;
        this.directoryPasswordPort = directoryPasswordPort;
        this.passwordAuditPort = passwordAuditPort;
        this.passwordPolicyService = passwordPolicyService;
    }

    @Override
    public Mono<PasswordChangeResult> resetPassword(String token, String newPassword,
                                                     String ipAddress, String userAgent) {
        log.debug("Processing password reset");

        return passwordResetTokenPort.findByToken(token)
                .switchIfEmpty(Mono.error(new PasswordResetTokenInvalidException()))
                .flatMap(resetToken -> {
                    if (resetToken.isUsed()) {
                        return Mono.error(new PasswordResetTokenInvalidException("Token already used"));
                    }
                    if (resetToken.isExpired()) {
                        return Mono.error(new PasswordResetTokenExpiredException(token));
                    }

                    // Validate new password against policy
                    passwordPolicyService.validatePassword(newPassword);

                    // Change password in directory service
                    return directoryPasswordPort.resetPassword(resetToken.userId(), newPassword)
                            .then(passwordResetTokenPort.markAsUsed(token))
                            .thenReturn(PasswordChangeResult.success(
                                    resetToken.userId(),
                                    PasswordChangeResult.ChangeType.RESET))
                            .doOnNext(result -> recordAuditSuccess(
                                    resetToken.userId(), resetToken.email().value(), ipAddress, userAgent));
                })
                .onErrorResume(e -> {
                    log.error("Password reset failed: {}", e.getMessage());
                    if (e instanceof PasswordResetTokenInvalidException ||
                        e instanceof PasswordResetTokenExpiredException) {
                        return Mono.error(e);
                    }
                    recordAuditFailure(e.getMessage(), ipAddress, userAgent);
                    return Mono.error(e);
                });
    }

    private void recordAuditSuccess(String userId, String email, String ipAddress, String userAgent) {
        passwordAuditPort.recordEvent(
                        PasswordAuditPort.EventType.PASSWORD_RESET_COMPLETED,
                        userId, email, true, null, ipAddress, userAgent)
                .subscribe(
                        null,
                        error -> log.warn("Failed to record password reset success audit", error)
                );
    }

    private void recordAuditFailure(String failureReason, String ipAddress, String userAgent) {
        passwordAuditPort.recordEvent(
                        PasswordAuditPort.EventType.PASSWORD_RESET_FAILED,
                        null, null, false, failureReason, ipAddress, userAgent)
                .subscribe(
                        null,
                        error -> log.warn("Failed to record password reset failure audit", error)
                );
    }
}
