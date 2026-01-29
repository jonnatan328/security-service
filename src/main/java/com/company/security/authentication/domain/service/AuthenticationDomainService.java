package com.company.security.authentication.domain.service;

import com.company.security.authentication.domain.exception.AccountDisabledException;
import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.Credentials;

/**
 * Domain service containing authentication business rules.
 * Pure domain logic with no external dependencies.
 */
public final class AuthenticationDomainService {

    /**
     * Validates that the authenticated user is allowed to sign in.
     *
     * @param user        the authenticated user from the directory service
     * @param credentials the original credentials used for authentication
     * @throws AccountDisabledException if the user account is disabled
     */
    public void validateUserCanSignIn(AuthenticatedUser user, Credentials credentials) {
        if (!user.enabled()) {
            throw new AccountDisabledException(credentials.username());
        }
    }

    /**
     * Validates that a token JTI is in valid format.
     *
     * @param jti the JWT ID to validate
     * @return true if the JTI is valid, false otherwise
     */
    public boolean isValidJti(String jti) {
        return jti != null && !jti.isBlank() && jti.length() >= 16;
    }

    /**
     * Validates that a refresh token request is valid.
     *
     * @param userId   the user ID from the refresh token
     * @param deviceId the device ID from the refresh token
     * @return true if the refresh request is valid
     */
    public boolean isValidRefreshRequest(String userId, String deviceId) {
        return userId != null && !userId.isBlank()
               && deviceId != null && !deviceId.isBlank();
    }
}
