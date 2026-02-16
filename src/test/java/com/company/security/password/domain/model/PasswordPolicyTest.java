package com.company.security.password.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PasswordPolicy Value Object")
class PasswordPolicyTest {

    @Test
    @DisplayName("Should build policy with valid values")
    void shouldBuildWithValidValues() {
        PasswordPolicy policy = PasswordPolicy.builder()
                .minLength(10)
                .maxLength(64)
                .requireUppercase(true)
                .requireLowercase(false)
                .requireDigit(true)
                .requireSpecialChar(false)
                .maxHistoryCount(3)
                .build();

        assertEquals(10, policy.minLength());
        assertEquals(64, policy.maxLength());
        assertTrue(policy.requireUppercase());
        assertFalse(policy.requireLowercase());
        assertTrue(policy.requireDigit());
        assertFalse(policy.requireSpecialChar());
        assertEquals(3, policy.maxHistoryCount());
    }

    @Test
    @DisplayName("Should create default policy with expected values")
    void shouldCreateDefaultPolicy() {
        PasswordPolicy policy = PasswordPolicy.defaultPolicy();

        assertEquals(8, policy.minLength());
        assertEquals(128, policy.maxLength());
        assertTrue(policy.requireUppercase());
        assertTrue(policy.requireLowercase());
        assertTrue(policy.requireDigit());
        assertTrue(policy.requireSpecialChar());
        assertEquals(5, policy.maxHistoryCount());
    }

    @Test
    @DisplayName("Should throw when minLength is less than 1")
    void shouldThrowWhenMinLengthLessThan1() {
        PasswordPolicy.Builder builder = PasswordPolicy.builder().minLength(0);
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    @DisplayName("Should throw when maxLength is less than minLength")
    void shouldThrowWhenMaxLengthLessThanMinLength() {
        PasswordPolicy.Builder builder = PasswordPolicy.builder().minLength(10).maxLength(5);
        assertThrows(IllegalArgumentException.class, builder::build);
    }
}
