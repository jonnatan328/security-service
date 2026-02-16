package com.company.security.token.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenTest {

    @Test
    void builder_withAllFields_createsToken() {
        Instant now = Instant.now();
        Token token = Token.builder()
                .rawToken("raw-jwt")
                .jti("jti-123")
                .subject("user-123")
                .userId("user-123")
                .email("john@company.com")
                .roles(Set.of("ROLE_USER"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .issuer("security-service")
                .build();

        assertThat(token.rawToken()).isEqualTo("raw-jwt");
        assertThat(token.jti()).isEqualTo("jti-123");
        assertThat(token.subject()).isEqualTo("user-123");
        assertThat(token.userId()).isEqualTo("user-123");
        assertThat(token.email()).isEqualTo("john@company.com");
        assertThat(token.roles()).contains("ROLE_USER");
        assertThat(token.issuedAt()).isEqualTo(now);
        assertThat(token.expiresAt()).isEqualTo(now.plusSeconds(900));
        assertThat(token.issuer()).isEqualTo("security-service");
    }

    @Test
    void builder_withNullRoles_returnsEmptySet() {
        Token token = Token.builder()
                .rawToken("raw-jwt")
                .jti("jti-123")
                .subject("user-123")
                .build();

        assertThat(token.roles()).isEmpty();
    }

    @Test
    void isExpired_withFutureExpiry_returnsFalse() {
        Token token = Token.builder()
                .rawToken("raw-jwt")
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        assertThat(token.isExpired()).isFalse();
    }

    @Test
    void isExpired_withPastExpiry_returnsTrue() {
        Token token = Token.builder()
                .rawToken("raw-jwt")
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().minusSeconds(100))
                .build();

        assertThat(token.isExpired()).isTrue();
    }

    @Test
    void isExpired_withNullExpiry_returnsFalse() {
        Token token = Token.builder()
                .rawToken("raw-jwt")
                .jti("jti-123")
                .subject("user-123")
                .build();

        assertThat(token.isExpired()).isFalse();
    }

    @Test
    void remainingTimeInSeconds_withFutureExpiry_returnsPositive() {
        Token token = Token.builder()
                .rawToken("raw-jwt")
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        assertThat(token.remainingTimeInSeconds()).isPositive();
    }

    @Test
    void remainingTimeInSeconds_withPastExpiry_returnsZero() {
        Token token = Token.builder()
                .rawToken("raw-jwt")
                .jti("jti-123")
                .subject("user-123")
                .expiresAt(Instant.now().minusSeconds(100))
                .build();

        assertThat(token.remainingTimeInSeconds()).isZero();
    }

    @Test
    void remainingTimeInSeconds_withNullExpiry_returnsZero() {
        Token token = Token.builder()
                .rawToken("raw-jwt")
                .jti("jti-123")
                .subject("user-123")
                .build();

        assertThat(token.remainingTimeInSeconds()).isZero();
    }

    @Test
    void equals_withSameJti_returnsTrue() {
        Token token1 = Token.builder().rawToken("raw1").jti("jti-123").subject("user-123").build();
        Token token2 = Token.builder().rawToken("raw2").jti("jti-123").subject("user-456").build();

        assertThat(token1).isEqualTo(token2).hasSameHashCodeAs(token2);
    }

    @Test
    void equals_withDifferentJti_returnsFalse() {
        Token token1 = Token.builder().rawToken("raw1").jti("jti-123").subject("user-123").build();
        Token token2 = Token.builder().rawToken("raw2").jti("jti-456").subject("user-123").build();

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void equals_withNull_returnsFalse() {
        Token token = Token.builder().rawToken("raw").jti("jti-123").subject("user-123").build();
        assertThat(token).isNotEqualTo(null);
    }

    @Test
    void equals_withSameInstance_returnsTrue() {
        Token token = Token.builder().rawToken("raw").jti("jti-123").subject("user-123").build();
        assertThat(token).isEqualTo(token);
    }

    @Test
    void toString_containsRelevantInfo() {
        Token token = Token.builder()
                .rawToken("raw")
                .jti("jti-123")
                .subject("user-123")
                .userId("user-123")
                .build();

        assertThat(token.toString()).contains("jti-123", "user-123");
    }

    @Test
    void builder_withNullRawToken_throwsException() {
        Token.Builder builder = Token.builder().jti("jti-123").subject("user-123");
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builder_withNullJti_throwsException() {
        Token.Builder builder = Token.builder().rawToken("raw").subject("user-123");
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builder_withNullSubject_throwsException() {
        Token.Builder builder = Token.builder().rawToken("raw").jti("jti-123");
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }
}
