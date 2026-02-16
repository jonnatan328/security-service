package com.company.security.token.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenValidationResultExtendedTest {

    @Test
    void valid_withToken_createsValidResult() {
        Token token = Token.builder()
                .rawToken("raw-jwt")
                .jti("jti-123")
                .subject("john.doe")
                .userId("user-123")
                .email("john@company.com")
                .roles(Set.of("ROLE_USER"))
                .expiresAt(Instant.now().plusSeconds(900))
                .build();

        TokenValidationResult result = TokenValidationResult.valid(token);

        assertThat(result.valid()).isTrue();
        assertThat(result.status()).isEqualTo(TokenStatus.VALID);
        assertThat(result.userId()).isEqualTo("user-123");
        assertThat(result.username()).isEqualTo("john.doe");
        assertThat(result.email()).isEqualTo("john@company.com");
        assertThat(result.roles()).contains("ROLE_USER");
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void expired_createsExpiredResult() {
        TokenValidationResult result = TokenValidationResult.expired();

        assertThat(result.valid()).isFalse();
        assertThat(result.status()).isEqualTo(TokenStatus.EXPIRED);
        assertThat(result.errorMessage()).isEqualTo("Token has expired");
    }

    @Test
    void revoked_createsRevokedResult() {
        TokenValidationResult result = TokenValidationResult.revoked();

        assertThat(result.valid()).isFalse();
        assertThat(result.status()).isEqualTo(TokenStatus.REVOKED);
        assertThat(result.errorMessage()).isEqualTo("Token has been revoked");
    }

    @Test
    void malformed_createsMalformedResult() {
        TokenValidationResult result = TokenValidationResult.malformed();

        assertThat(result.valid()).isFalse();
        assertThat(result.status()).isEqualTo(TokenStatus.MALFORMED);
        assertThat(result.errorMessage()).isEqualTo("Token is malformed");
    }

    @Test
    void invalid_createsInvalidResult() {
        TokenValidationResult result = TokenValidationResult.invalid(TokenStatus.INVALID, "Custom error");

        assertThat(result.valid()).isFalse();
        assertThat(result.status()).isEqualTo(TokenStatus.INVALID);
        assertThat(result.errorMessage()).isEqualTo("Custom error");
    }

    @Test
    void equals_withSameValues_returnsTrue() {
        TokenValidationResult result1 = TokenValidationResult.builder()
                .valid(true)
                .status(TokenStatus.VALID)
                .userId("user-123")
                .build();

        TokenValidationResult result2 = TokenValidationResult.builder()
                .valid(true)
                .status(TokenStatus.VALID)
                .userId("user-123")
                .build();

        assertThat(result1).isEqualTo(result2).hasSameHashCodeAs(result2);
    }

    @Test
    void equals_withSameInstance_returnsTrue() {
        TokenValidationResult result = TokenValidationResult.expired();
        assertThat(result).isEqualTo(result);
    }

    @Test
    void equals_withNull_returnsFalse() {
        TokenValidationResult result = TokenValidationResult.expired();
        assertThat(result).isNotEqualTo(null);
    }

    @Test
    void toString_containsRelevantInfo() {
        TokenValidationResult result = TokenValidationResult.builder()
                .valid(true)
                .status(TokenStatus.VALID)
                .userId("user-123")
                .build();
        assertThat(result.toString()).contains("user-123", "VALID");
    }

    @Test
    void builder_withNullStatus_throwsException() {
        TokenValidationResult.Builder builder = TokenValidationResult.builder()
                .valid(true);
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builder_withNullRoles_returnsEmptySet() {
        TokenValidationResult result = TokenValidationResult.builder()
                .valid(true)
                .status(TokenStatus.VALID)
                .build();

        assertThat(result.roles()).isEmpty();
    }
}
