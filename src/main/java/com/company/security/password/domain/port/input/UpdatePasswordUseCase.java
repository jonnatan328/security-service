package com.company.security.password.domain.port.input;

import com.company.security.password.domain.model.PasswordChangeResult;
import reactor.core.publisher.Mono;

/**
 * Input port for password update use case.
 * Allows authenticated users to change their password.
 */
public interface UpdatePasswordUseCase {

    /**
     * Updates the user's password (requires current password).
     *
     * @param userId          the authenticated user ID
     * @param currentPassword the current password for verification
     * @param newPassword     the new password
     * @param ipAddress       the client IP address for audit purposes
     * @param userAgent       the client user agent for audit purposes
     * @return a Mono containing the result of the password change
     */
    Mono<PasswordChangeResult> updatePassword(
            String userId, String currentPassword, String newPassword,
            String ipAddress, String userAgent);
}
