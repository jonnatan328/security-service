package com.company.security.authentication.infrastructure.adapter.input.rest.handler;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.AuthenticationResult;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.authentication.domain.model.TokenPair;
import com.company.security.authentication.domain.port.input.RefreshTokenUseCase;
import com.company.security.authentication.domain.port.input.SignInUseCase;
import com.company.security.authentication.domain.port.input.SignOutUseCase;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.RefreshTokenRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignInRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.AuthenticationResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.TokenResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.mapper.AuthenticationRestMapper;
import com.company.security.shared.domain.model.Email;
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
class AuthenticationHandlerTest {

    @Mock
    private SignInUseCase signInUseCase;

    @Mock
    private SignOutUseCase signOutUseCase;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @Mock
    private AuthenticationRestMapper mapper;

    private AuthenticationHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AuthenticationHandler(signInUseCase, signOutUseCase, refreshTokenUseCase, mapper);
    }

    @Test
    void signIn_withValidRequest_returnsAuthenticationResponse() {
        SignInRequest request = new SignInRequest("john.doe", "password123");
        Credentials credentials = Credentials.of("john.doe", "password123", "device-001");
        AuthenticationResult result = buildAuthenticationResult();
        AuthenticationResponse response = buildAuthenticationResponse();

        when(mapper.toCredentials(request, "device-001")).thenReturn(credentials);
        when(signInUseCase.signIn(credentials, "192.168.1.1", "TestAgent"))
                .thenReturn(Mono.just(result));
        when(mapper.toAuthenticationResponse(result)).thenReturn(response);

        StepVerifier.create(handler.signIn(request, "device-001", "192.168.1.1", "TestAgent"))
                .assertNext(resp -> assertThat(resp.accessToken()).isEqualTo("access-token"))
                .verifyComplete();
    }

    @Test
    void signOut_completesSuccessfully() {
        when(signOutUseCase.signOut("access-token", "refresh-token", "192.168.1.1", "TestAgent"))
                .thenReturn(Mono.empty());

        StepVerifier.create(handler.signOut("access-token", "refresh-token", "192.168.1.1", "TestAgent"))
                .verifyComplete();
    }

    @Test
    void refresh_withValidRequest_returnsTokenResponse() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        TokenPair tokenPair = buildTokenPair();
        TokenResponse response = new TokenResponse("new-access", "new-refresh", "Bearer", 900);

        when(refreshTokenUseCase.refreshToken("refresh-token", "192.168.1.1", "TestAgent"))
                .thenReturn(Mono.just(tokenPair));
        when(mapper.toTokenResponse(tokenPair)).thenReturn(response);

        StepVerifier.create(handler.refresh(request, "192.168.1.1", "TestAgent"))
                .assertNext(resp -> assertThat(resp.accessToken()).isEqualTo("new-access"))
                .verifyComplete();
    }

    private AuthenticationResult buildAuthenticationResult() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .build();
        return AuthenticationResult.of(user, buildTokenPair());
    }

    private TokenPair buildTokenPair() {
        return TokenPair.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .accessTokenExpiresAt(Instant.now().plusSeconds(900))
                .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                .tokenType("Bearer")
                .build();
    }

    private AuthenticationResponse buildAuthenticationResponse() {
        return new AuthenticationResponse(
                "access-token", "refresh-token", "Bearer", 900,
                new AuthenticationResponse.UserInfo("user-123", "john.doe", "john@company.com", "John", "Doe", Set.of("ROLE_USER"))
        );
    }
}
