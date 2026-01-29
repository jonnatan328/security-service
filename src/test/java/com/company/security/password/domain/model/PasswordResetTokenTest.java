package com.company.security.password.domain.model;

import com.company.security.shared.domain.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordResetToken Value Object")
class PasswordResetTokenTest {

    @Test
    @DisplayName("Should create a valid token via create() factory method")
    void shouldCreateValidToken() {
        Email email = Email.of("user@example.com");
        PasswordResetToken token = PasswordResetToken.create("user-1", email, 30);

        assertNotNull(token.id());
        assertNotNull(token.token());
        assertEquals("user-1", token.userId());
        assertEquals(email, token.email());
        assertNotNull(token.createdAt());
        assertNotNull(token.expiresAt());
        assertEquals(PasswordResetToken.Status.PENDING, token.status());
    }

    @Test
    @DisplayName("Should be valid when pending and not expired")
    void shouldBeValidWhenPendingAndNotExpired() {
        PasswordResetToken token = PasswordResetToken.create("user-1", Email.of("u@test.com"), 30);

        assertTrue(token.isValid());
        assertTrue(token.isPending());
        assertFalse(token.isExpired());
        assertFalse(token.isUsed());
    }

    @Test
    @DisplayName("Should be expired when expiresAt is in the past")
    void shouldBeExpiredWhenPast() {
        PasswordResetToken token = PasswordResetToken.builder()
                .id("id-1")
                .token("token-1")
                .userId("user-1")
                .email(Email.of("u@test.com"))
                .createdAt(Instant.now().minusSeconds(7200))
                .expiresAt(Instant.now().minusSeconds(3600))
                .status(PasswordResetToken.Status.PENDING)
                .build();

        assertTrue(token.isExpired());
        assertFalse(token.isValid());
    }

    @Test
    @DisplayName("Should transition to USED status via markAsUsed()")
    void shouldMarkAsUsed() {
        PasswordResetToken token = PasswordResetToken.create("user-1", Email.of("u@test.com"), 30);
        PasswordResetToken used = token.markAsUsed();

        assertEquals(PasswordResetToken.Status.USED, used.status());
        assertTrue(used.isUsed());
        assertNotNull(used.usedAt());
        assertFalse(used.isValid());
    }

    @Test
    @DisplayName("Should transition to CANCELLED status via markAsCancelled()")
    void shouldMarkAsCancelled() {
        PasswordResetToken token = PasswordResetToken.create("user-1", Email.of("u@test.com"), 30);
        PasswordResetToken cancelled = token.markAsCancelled();

        assertEquals(PasswordResetToken.Status.CANCELLED, cancelled.status());
        assertFalse(cancelled.isValid());
    }

    @Test
    @DisplayName("Should preserve token identity after marking as used")
    void shouldPreserveIdentityAfterMarkAsUsed() {
        PasswordResetToken original = PasswordResetToken.create("user-1", Email.of("u@test.com"), 30);
        PasswordResetToken used = original.markAsUsed();

        assertEquals(original.token(), used.token());
        assertEquals(original, used);
    }
}
