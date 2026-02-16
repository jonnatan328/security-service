package com.company.security.token.infrastructure.adapter.input.rest.handler;

import com.company.security.token.domain.model.TokenStatus;
import com.company.security.token.domain.model.TokenValidationResult;
import com.company.security.token.domain.port.input.ValidateTokenUseCase;
import com.company.security.token.infrastructure.adapter.input.rest.dto.request.ValidateTokenRequest;
import com.company.security.token.infrastructure.adapter.input.rest.dto.response.TokenValidationResponse;
import com.company.security.token.infrastructure.adapter.input.rest.mapper.TokenRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenValidationHandlerTest {

    @Mock
    private ValidateTokenUseCase validateTokenUseCase;

    @Mock
    private TokenRestMapper mapper;

    private TokenValidationHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TokenValidationHandler(validateTokenUseCase, mapper);
    }

    @Test
    void validate_withValidToken_returnsValidResponse() {
        ValidateTokenRequest request = new ValidateTokenRequest("valid-token");
        TokenValidationResult result = TokenValidationResult.builder()
                .valid(true)
                .status(TokenStatus.VALID)
                .userId("user-123")
                .username("john.doe")
                .email("john@company.com")
                .roles(Set.of("ROLE_USER"))
                .expiresAt(Instant.now().plusSeconds(900))
                .build();
        TokenValidationResponse response = new TokenValidationResponse(
                true, "VALID", "user-123", "john.doe", "john@company.com",
                Set.of("ROLE_USER"), Instant.now().plusSeconds(900), null);

        when(validateTokenUseCase.validate("valid-token")).thenReturn(Mono.just(result));
        when(mapper.toResponse(result)).thenReturn(response);

        StepVerifier.create(handler.validate(request, "192.168.1.1"))
                .assertNext(resp -> assertThat(resp.valid()).isTrue())
                .verifyComplete();
    }
}
