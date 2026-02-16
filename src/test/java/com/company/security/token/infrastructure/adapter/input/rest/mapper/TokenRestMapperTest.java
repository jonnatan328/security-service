package com.company.security.token.infrastructure.adapter.input.rest.mapper;

import com.company.security.token.domain.model.TokenStatus;
import com.company.security.token.domain.model.TokenValidationResult;
import com.company.security.token.infrastructure.adapter.input.rest.dto.response.TokenValidationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TokenRestMapperTest {

    private TokenRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TokenRestMapper();
    }

    @Test
    void toResponse_withValidResult_mapsCorrectly() {
        Instant expiresAt = Instant.now().plusSeconds(900);
        TokenValidationResult result = TokenValidationResult.builder()
                .valid(true)
                .status(TokenStatus.VALID)
                .userId("user-123")
                .username("john.doe")
                .email("john@company.com")
                .roles(Set.of("ROLE_USER"))
                .expiresAt(expiresAt)
                .build();

        TokenValidationResponse response = mapper.toResponse(result);

        assertThat(response.valid()).isTrue();
        assertThat(response.status()).isEqualTo("VALID");
        assertThat(response.userId()).isEqualTo("user-123");
        assertThat(response.username()).isEqualTo("john.doe");
        assertThat(response.email()).isEqualTo("john@company.com");
        assertThat(response.roles()).contains("ROLE_USER");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        assertThat(response.errorMessage()).isNull();
    }

    @Test
    void toResponse_withInvalidResult_mapsCorrectly() {
        TokenValidationResult result = TokenValidationResult.invalid(TokenStatus.EXPIRED, "Token has expired");

        TokenValidationResponse response = mapper.toResponse(result);

        assertThat(response.valid()).isFalse();
        assertThat(response.status()).isEqualTo("EXPIRED");
        assertThat(response.errorMessage()).isEqualTo("Token has expired");
        assertThat(response.userId()).isNull();
    }
}
