package com.company.security.shared.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Email Value Object")
class EmailTest {

    @Test
    @DisplayName("Should create email with valid address")
    void shouldCreateEmailWithValidAddress() {
        Email email = Email.of("user@example.com");

        assertNotNull(email);
        assertEquals("user@example.com", email.value());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid email format")
    void shouldThrowForInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> Email.of("not-an-email"));
        assertThrows(IllegalArgumentException.class, () -> Email.of("missing@"));
        assertThrows(IllegalArgumentException.class, () -> Email.of("@domain.com"));
        assertThrows(IllegalArgumentException.class, () -> Email.of("spaces in@email.com"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null email")
    void shouldThrowForNull() {
        assertThrows(IllegalArgumentException.class, () -> Email.of(null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank email")
    void shouldThrowForBlank() {
        assertThrows(IllegalArgumentException.class, () -> Email.of(""));
        assertThrows(IllegalArgumentException.class, () -> Email.of("   "));
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    void shouldNormalizeToLowercase() {
        Email email = Email.of("User@Example.COM");

        assertEquals("user@example.com", email.value());
    }

    @Test
    @DisplayName("Should extract domain correctly")
    void shouldExtractDomain() {
        Email email = Email.of("user@example.com");

        assertEquals("example.com", email.domain());
    }

    @Test
    @DisplayName("Should extract local part correctly")
    void shouldExtractLocalPart() {
        Email email = Email.of("user@example.com");

        assertEquals("user", email.localPart());
    }

    @Test
    @DisplayName("Should be equal for same email value")
    void shouldBeEqualForSameValue() {
        Email email1 = Email.of("user@example.com");
        Email email2 = Email.of("user@example.com");

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal for different email values")
    void shouldNotBeEqualForDifferentValues() {
        Email email1 = Email.of("user1@example.com");
        Email email2 = Email.of("user2@example.com");

        assertNotEquals(email1, email2);
    }

    @Test
    @DisplayName("Should treat case-different emails as equal due to normalization")
    void shouldTreatCaseDifferentAsEqual() {
        Email email1 = Email.of("User@Example.com");
        Email email2 = Email.of("user@example.com");

        assertEquals(email1, email2);
    }
}
