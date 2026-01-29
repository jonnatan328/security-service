package com.company.security.authentication.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Credentials Value Object")
class CredentialsTest {

    @Test
    @DisplayName("Should create valid credentials with username, password and deviceId")
    void shouldCreateValidCredentials() {
        Credentials credentials = Credentials.of("admin", "secret123", "device-1");

        assertEquals("admin", credentials.username());
        assertEquals("secret123", credentials.password());
        assertEquals("device-1", credentials.deviceId());
    }

    @Test
    @DisplayName("Should throw NullPointerException for null username")
    void shouldThrowForNullUsername() {
        assertThrows(NullPointerException.class, () -> Credentials.of(null, "password"));
    }

    @Test
    @DisplayName("Should throw NullPointerException for null password")
    void shouldThrowForNullPassword() {
        assertThrows(NullPointerException.class, () -> Credentials.of("user", null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank username")
    void shouldThrowForBlankUsername() {
        assertThrows(IllegalArgumentException.class, () -> Credentials.of("   ", "password"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank password")
    void shouldThrowForBlankPassword() {
        assertThrows(IllegalArgumentException.class, () -> Credentials.of("user", "   "));
    }

    @Test
    @DisplayName("Should default deviceId to 'default' when not provided")
    void shouldDefaultDeviceId() {
        Credentials credentials = Credentials.of("admin", "secret123");

        assertEquals("default", credentials.deviceId());
    }

    @Test
    @DisplayName("Should default deviceId to 'default' when null")
    void shouldDefaultDeviceIdWhenNull() {
        Credentials credentials = Credentials.of("admin", "secret123", null);

        assertEquals("default", credentials.deviceId());
    }

    @Test
    @DisplayName("Should be equal when username and deviceId match")
    void shouldBeEqualWhenUsernameAndDeviceIdMatch() {
        Credentials c1 = Credentials.of("admin", "pass1", "device-1");
        Credentials c2 = Credentials.of("admin", "pass2", "device-1");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when username differs")
    void shouldNotBeEqualWhenUsernameDiffers() {
        Credentials c1 = Credentials.of("admin", "pass", "device-1");
        Credentials c2 = Credentials.of("user", "pass", "device-1");

        assertNotEquals(c1, c2);
    }
}
