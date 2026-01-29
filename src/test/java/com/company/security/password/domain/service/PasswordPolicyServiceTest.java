package com.company.security.password.domain.service;

import com.company.security.password.domain.exception.PasswordValidationException;
import com.company.security.password.domain.model.Password;
import com.company.security.password.domain.model.PasswordPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordPolicyService")
class PasswordPolicyServiceTest {

    private PasswordPolicyService service;

    @BeforeEach
    void setUp() {
        PasswordPolicy policy = PasswordPolicy.builder()
                .minLength(8)
                .maxLength(128)
                .requireUppercase(true)
                .requireLowercase(true)
                .requireDigit(true)
                .requireSpecialChar(true)
                .build();
        service = new PasswordPolicyService(policy);
    }

    @Test
    @DisplayName("Should accept a valid password")
    void shouldAcceptValidPassword() {
        assertDoesNotThrow(() -> service.validatePassword("Str0ng!Pass"));
    }

    @Test
    @DisplayName("Should reject password that is too short")
    void shouldRejectTooShortPassword() {
        PasswordValidationException ex = assertThrows(PasswordValidationException.class,
                () -> service.validatePassword("Ab1!"));

        assertTrue(ex.violations().stream()
                .anyMatch(v -> v.contains("at least 8 characters")));
    }

    @Test
    @DisplayName("Should reject password without uppercase letter")
    void shouldRejectWithoutUppercase() {
        PasswordValidationException ex = assertThrows(PasswordValidationException.class,
                () -> service.validatePassword("lowercase1!only"));

        assertTrue(ex.violations().stream()
                .anyMatch(v -> v.contains("uppercase")));
    }

    @Test
    @DisplayName("Should reject password without digit")
    void shouldRejectWithoutDigit() {
        PasswordValidationException ex = assertThrows(PasswordValidationException.class,
                () -> service.validatePassword("NoDigits!Here"));

        assertTrue(ex.violations().stream()
                .anyMatch(v -> v.contains("digit")));
    }

    @Test
    @DisplayName("Should reject password without special character")
    void shouldRejectWithoutSpecialChar() {
        PasswordValidationException ex = assertThrows(PasswordValidationException.class,
                () -> service.validatePassword("NoSpecial1Here"));

        assertTrue(ex.violations().stream()
                .anyMatch(v -> v.contains("special")));
    }

    @Test
    @DisplayName("Should create validated Password object for valid password")
    void shouldCreateValidatedPassword() {
        Password password = service.createValidatedPassword("Str0ng!Pass");

        assertNotNull(password);
        assertEquals("Str0ng!Pass", password.value());
    }

    @Test
    @DisplayName("Should throw when creating validated password with invalid input")
    void shouldThrowWhenCreatingInvalidPassword() {
        assertThrows(PasswordValidationException.class,
                () -> service.createValidatedPassword("weak"));
    }
}
