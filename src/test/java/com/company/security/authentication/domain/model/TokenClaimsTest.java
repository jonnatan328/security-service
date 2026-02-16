package com.company.security.authentication.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenClaimsTest {

    @Test
    void builder_withAllFields_createsClaims() {
        Instant now = Instant.now();
        TokenClaims claims = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .userId("user-123")
                .username("john.doe")
                .email("john@company.com")
                .roles(Set.of("ROLE_USER"))
                .deviceId("device-001")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .issuer("security-service")
                .additionalClaims(Map.of("custom", "value"))
                .build();

        assertThat(claims.jti()).isEqualTo("jti-123");
        assertThat(claims.subject()).isEqualTo("user-123");
        assertThat(claims.userId()).isEqualTo("user-123");
        assertThat(claims.username()).isEqualTo("john.doe");
        assertThat(claims.email()).isEqualTo("john@company.com");
        assertThat(claims.roles()).contains("ROLE_USER");
        assertThat(claims.deviceId()).isEqualTo("device-001");
        assertThat(claims.issuedAt()).isEqualTo(now);
        assertThat(claims.expiresAt()).isEqualTo(now.plusSeconds(900));
        assertThat(claims.issuer()).isEqualTo("security-service");
        assertThat(claims.additionalClaims()).containsEntry("custom", "value");
    }

    @Test
    void builder_withNullRoles_returnsEmptySet() {
        TokenClaims claims = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        assertThat(claims.roles()).isEmpty();
        assertThat(claims.additionalClaims()).isEmpty();
    }

    @Test
    void isExpired_withFutureExpiry_returnsFalse() {
        TokenClaims claims = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        assertThat(claims.isExpired()).isFalse();
    }

    @Test
    void isExpired_withPastExpiry_returnsTrue() {
        TokenClaims claims = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().minusSeconds(100))
                .build();

        assertThat(claims.isExpired()).isTrue();
    }

    @Test
    void remainingTimeInSeconds_withFutureExpiry_returnsPositive() {
        TokenClaims claims = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        assertThat(claims.remainingTimeInSeconds()).isPositive();
    }

    @Test
    void remainingTimeInSeconds_withPastExpiry_returnsZero() {
        TokenClaims claims = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().minusSeconds(100))
                .build();

        assertThat(claims.remainingTimeInSeconds()).isZero();
    }

    @Test
    void hasRole_withExistingRole_returnsTrue() {
        TokenClaims claims = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .roles(Set.of("ROLE_USER"))
                .build();

        assertThat(claims.hasRole("ROLE_USER")).isTrue();
        assertThat(claims.hasRole("ROLE_ADMIN")).isFalse();
    }

    @Test
    void equals_withSameJti_returnsTrue() {
        TokenClaims claims1 = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        TokenClaims claims2 = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-456")
                .expiresAt(Instant.now().plusSeconds(1800))
                .build();

        assertThat(claims1).isEqualTo(claims2).hasSameHashCodeAs(claims2);
    }

    @Test
    void equals_withDifferentJti_returnsFalse() {
        TokenClaims claims1 = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        TokenClaims claims2 = TokenClaims.builder()
                .jti("jti-456")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        assertThat(claims1).isNotEqualTo(claims2);
    }

    @Test
    void equals_withNull_returnsFalse() {
        TokenClaims claims = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        assertThat(claims).isNotEqualTo(null);
    }

    @Test
    void equals_withSameInstance_returnsTrue() {
        TokenClaims claims = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        assertThat(claims).isEqualTo(claims);
    }

    @Test
    void toString_containsRelevantInfo() {
        TokenClaims claims = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123")
                .userId("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        assertThat(claims.toString()).contains("jti-123", "user-123");
    }

    @Test
    void builder_withNullJti_throwsException() {
        TokenClaims.Builder builder = TokenClaims.builder()
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900));
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builder_withNullSubject_throwsException() {
        TokenClaims.Builder builder = TokenClaims.builder()
                .jti("jti-123")
                .expiresAt(Instant.now().plusSeconds(900));
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builder_withNullExpiresAt_throwsException() {
        TokenClaims.Builder builder = TokenClaims.builder()
                .jti("jti-123")
                .subject("user-123");
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }
}
