package com.company.security.password.domain.port.input;

import com.company.security.password.domain.model.PasswordChangeResult;
import reactor.core.publisher.Mono;

/**
 * Input port for password reset use case.
 * Allows setting a new password using a recovery token.
 */
public interface ResetPasswordUseCase {

    /**
     * Resets the user's password using a recovery token.
     *
     * @param token       the password reset token
     * @param newPassword the new password
     * @param ipAddress   the client IP address for audit purposes
     * @param userAgent   the client user agent for audit purposes
     * @return a Mono containing the result of the password change
     */
    Mono<PasswordChangeResult> resetPassword(String token, String newPassword, String ipAddress, String userAgent);
}
