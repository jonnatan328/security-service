package com.company.security.authentication.domain.service;

import com.company.security.authentication.domain.exception.AccountDisabledException;
import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.shared.domain.model.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AuthenticationDomainService")
class AuthenticationDomainServiceTest {

    private AuthenticationDomainService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationDomainService();
    }

    @Test
    @DisplayName("Should not throw when user is enabled")
    void shouldNotThrowWhenUserIsEnabled() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-1")
                .username("admin")
                .email(Email.of("admin@example.com"))
                .enabled(true)
                .build();
        Credentials credentials = Credentials.of("admin", "password");

        assertDoesNotThrow(() -> service.validateUserCanSignIn(user, credentials));
    }

    @Test
    @DisplayName("Should throw AccountDisabledException when user is disabled")
    void shouldThrowWhenUserIsDisabled() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-1")
                .username("admin")
                .email(Email.of("admin@example.com"))
                .enabled(false)
                .build();
        Credentials credentials = Credentials.of("admin", "password");

        assertThrows(AccountDisabledException.class,
                () -> service.validateUserCanSignIn(user, credentials));
    }

    @Test
    @DisplayName("Should return true for valid JTI with 16+ characters")
    void shouldReturnTrueForValidJti() {
        assertTrue(service.isValidJti("abcdefghijklmnop"));
        assertTrue(service.isValidJti("1234567890abcdef1234"));
    }

    @Test
    @DisplayName("Should return false for invalid JTI")
    void shouldReturnFalseForInvalidJti() {
        assertFalse(service.isValidJti(null));
        assertFalse(service.isValidJti(""));
        assertFalse(service.isValidJti("   "));
        assertFalse(service.isValidJti("short"));
    }

    @Test
    @DisplayName("Should return true for valid refresh request")
    void shouldReturnTrueForValidRefreshRequest() {
        assertTrue(service.isValidRefreshRequest("user-1", "device-1"));
    }

    @Test
    @DisplayName("Should return false for invalid refresh request")
    void shouldReturnFalseForInvalidRefreshRequest() {
        assertFalse(service.isValidRefreshRequest(null, "device-1"));
        assertFalse(service.isValidRefreshRequest("user-1", null));
        assertFalse(service.isValidRefreshRequest("", "device-1"));
        assertFalse(service.isValidRefreshRequest("user-1", ""));
        assertFalse(service.isValidRefreshRequest("   ", "device-1"));
        assertFalse(service.isValidRefreshRequest("user-1", "   "));
    }
}
