package com.company.security.password.infrastructure.application.usecase;

import com.company.security.password.domain.exception.CurrentPasswordMismatchException;
import com.company.security.password.domain.model.PasswordChangeResult;
import com.company.security.password.domain.service.PasswordPolicyService;
import com.company.security.password.infrastructure.application.port.input.UpdatePasswordUseCase;
import com.company.security.password.infrastructure.application.port.output.DirectoryPasswordPort;
import com.company.security.password.infrastructure.application.port.output.PasswordAuditPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the password update use case.
 */
@Service
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
                            .then(passwordAuditPort.recordEvent(
                                    PasswordAuditPort.EventType.PASSWORD_UPDATED,
                                    userId,
                                    null,
                                    true,
                                    null,
                                    ipAddress,
                                    userAgent))
                            .thenReturn(PasswordChangeResult.success(
                                    userId,
                                    PasswordChangeResult.ChangeType.UPDATE));
                })
                .doOnSuccess(result -> log.info("Password updated for user: {}", userId))
                .onErrorResume(CurrentPasswordMismatchException.class, Mono::error)
                .onErrorResume(e -> {
                    if (e instanceof CurrentPasswordMismatchException) {
                        return Mono.error(e);
                    }
                    log.error("Password update failed for user: {}", userId, e);
                    return Mono.error(e);
                });
    }
}
