package com.company.security.password.domain.usecase;

import com.company.security.password.domain.exception.CurrentPasswordMismatchException;
import com.company.security.password.domain.model.PasswordChangeResult;
import com.company.security.password.domain.service.PasswordPolicyService;
import com.company.security.password.domain.port.input.UpdatePasswordUseCase;
import com.company.security.password.domain.port.output.DirectoryPasswordPort;
import com.company.security.password.domain.port.output.PasswordAuditPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Implementation of the password update use case.
 */
public class UpdatePasswordUseCaseImpl implements UpdatePasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdatePasswordUseCaseImpl.class);

    private final DirectoryPasswordPort directoryPasswordPort;
    private final PasswordAuditPort passwordAuditPort;
    private final PasswordPolicyService passwordPolicyService;

    public UpdatePasswordUseCaseImpl(
            DirectoryPasswordPort directoryPasswordPort,
            PasswordAuditPort passwordAuditPort,
            PasswordPolicyService passwordPolicyService) {
        this.directoryPasswordPort = directoryPasswordPort;
        this.passwordAuditPort = passwordAuditPort;
        this.passwordPolicyService = passwordPolicyService;
    }

    @Override
    public Mono<PasswordChangeResult> updatePassword(
            String userId, String currentPassword, String newPassword,
            String ipAddress, String userAgent) {
        log.debug("Processing password update for user: {}", userId);

        return directoryPasswordPort.verifyPassword(userId, currentPassword)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new CurrentPasswordMismatchException(userId));
                    }

                    passwordPolicyService.validatePassword(newPassword);

                    return directoryPasswordPort.changePassword(userId, newPassword)
                            .thenReturn(PasswordChangeResult.success(
                                    userId,
                                    PasswordChangeResult.ChangeType.UPDATE))
                            .doOnNext(result -> recordAudit(userId, ipAddress, userAgent));
                })
                .doOnSuccess(result -> log.info("Password updated for user: {}", userId))
                .onErrorResume(e -> {
                    if (e instanceof CurrentPasswordMismatchException) {
                        return Mono.error(e);
                    }
                    log.error("Password update failed for user: {}", userId, e);
                    return Mono.error(e);
                });
    }

    private void recordAudit(String userId, String ipAddress, String userAgent) {
        passwordAuditPort.recordEvent(
                        PasswordAuditPort.EventType.PASSWORD_UPDATED,
                        userId, null, true, null, ipAddress, userAgent)
                .subscribe(
                        null,
                        error -> log.warn("Failed to record password update audit for user: {}", userId, error)
                );
    }
}
