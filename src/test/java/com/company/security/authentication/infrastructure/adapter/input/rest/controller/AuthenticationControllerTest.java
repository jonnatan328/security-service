package com.company.security.authentication.infrastructure.adapter.input.rest.controller;

import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.RefreshTokenRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignInRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignOutRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.AuthenticationResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.TokenResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.handler.AuthenticationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationHandler handler;

    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthenticationController(handler);
    }

    @Test
    void signIn_withValidRequest_returns200() {
        SignInRequest request = new SignInRequest("john.doe", "password");
        AuthenticationResponse response = new AuthenticationResponse(
                "access-token", "refresh-token", "Bearer", 900,
                new AuthenticationResponse.UserInfo("user-123", "john.doe", "john@company.com", "John", "Doe", Set.of("ROLE_USER")));

        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/auth/signin")
                .header("User-Agent", "TestAgent")
                .build();

        when(handler.signIn(eq(request), isNull(), anyString(), eq("TestAgent")))
                .thenReturn(Mono.just(response));

        StepVerifier.create(controller.signIn(request, null, httpRequest))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entity.getBody()).isNotNull();
                    assertThat(entity.getBody().accessToken()).isEqualTo("access-token");
                })
                .verifyComplete();
    }

    @Test
    void signIn_withXForwardedFor_extractsIp() {
        SignInRequest request = new SignInRequest("john.doe", "password");
        AuthenticationResponse response = new AuthenticationResponse(
                "access-token", "refresh-token", "Bearer", 900,
                new AuthenticationResponse.UserInfo("user-123", "john.doe", "john@company.com", "John", "Doe", Set.of()));

        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/auth/signin")
                .header("X-Forwarded-For", "10.0.0.1, 192.168.1.1")
                .build();

        when(handler.signIn(eq(request), isNull(), eq("10.0.0.1"), any()))
                .thenReturn(Mono.just(response));

        StepVerifier.create(controller.signIn(request, null, httpRequest))
                .assertNext(entity -> assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();
    }

    @Test
    void signOut_returns204() {
        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/auth/signout")
                .header("Authorization", "Bearer access-token-value")
                .build();

        when(handler.signOut(eq("access-token-value"), isNull(), anyString(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.signOut(null, "Bearer access-token-value", httpRequest))
                .assertNext(entity -> assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();
    }

    @Test
    void signOut_withRefreshToken_passesRefreshToken() {
        SignOutRequest signOutRequest = new SignOutRequest("refresh-token-value");
        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/auth/signout")
                .header("Authorization", "Bearer access-token-value")
                .build();

        when(handler.signOut(eq("access-token-value"), eq("refresh-token-value"), anyString(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.signOut(signOutRequest, "Bearer access-token-value", httpRequest))
                .assertNext(entity -> assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();
    }

    @Test
    void signOut_withInvalidBearerPrefix_returnsNullToken() {
        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/auth/signout")
                .header("Authorization", "Basic token")
                .build();

        when(handler.signOut(isNull(), isNull(), anyString(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.signOut(null, "Basic token", httpRequest))
                .assertNext(entity -> assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();
    }

    @Test
    void refresh_returns200() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        TokenResponse response = new TokenResponse("new-access", "new-refresh", "Bearer", 900);
        MockServerHttpRequest httpRequest = MockServerHttpRequest.post("/api/v1/auth/refresh").build();

        when(handler.refresh(eq(request), anyString(), any()))
                .thenReturn(Mono.just(response));

        StepVerifier.create(controller.refresh(request, httpRequest))
                .assertNext(entity -> {
                    assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(entity.getBody().accessToken()).isEqualTo("new-access");
                })
                .verifyComplete();
    }
}
