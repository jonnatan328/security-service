package com.company.security.token.infrastructure.adapter.input.rest.controller;

import com.company.security.token.infrastructure.adapter.input.rest.dto.request.ValidateTokenRequest;
import com.company.security.token.infrastructure.adapter.input.rest.dto.response.TokenValidationResponse;
import com.company.security.token.infrastructure.adapter.input.rest.handler.TokenValidationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenValidationControllerTest {

    @Mock
    private TokenValidationHandler handler;

    private TokenValidationController controller;

    @BeforeEach
    void setUp() {
        controller = new TokenValidationController(handler);
    }

    @Test
    void validate_returns200() {
        ValidateTokenRequest request = new ValidateTokenRequest("valid-token");
        TokenValidationResponse response = new TokenValidationResponse(
                true, "VALID", "user-123", "john.doe", "john@company.com",
                Set.of("ROLE_USER"), Instant.now().plusSeconds(900), null);
        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/internal/v1/token/validate").build();

        when(handler.validate(eq(request), anyString())).thenReturn(Mono.just(response));

        StepVerifier.create(controller.validate(request, httpRequest))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().valid()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void validate_withXForwardedFor_extractsIp() {
        ValidateTokenRequest request = new ValidateTokenRequest("valid-token");
        TokenValidationResponse response = new TokenValidationResponse(
                true, "VALID", "user-123", "john.doe", "john@company.com",
                Set.of("ROLE_USER"), Instant.now().plusSeconds(900), null);
        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/internal/v1/token/validate")
                .header("X-Forwarded-For", "10.0.0.1")
                .build();

        when(handler.validate(request, "10.0.0.1")).thenReturn(Mono.just(response));

        StepVerifier.create(controller.validate(request, httpRequest))
                .assertNext(entity -> assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
    }
}
