package com.company.security.authentication.domain.usecase;

import com.company.security.authentication.domain.exception.InvalidCredentialsException;
import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.AuthenticationResult;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.authentication.domain.model.TokenClaims;
import com.company.security.authentication.domain.model.TokenPair;
import com.company.security.authentication.domain.service.AuthenticationDomainService;
import com.company.security.authentication.domain.port.output.AuthAuditPort;
import com.company.security.authentication.domain.port.output.DirectoryServicePort;
import com.company.security.authentication.domain.port.output.RefreshTokenPort;
import com.company.security.authentication.domain.port.output.TokenProviderPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignInUseCaseImplTest {

    @Mock
    private DirectoryServicePort directoryServicePort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @Mock
    private AuthAuditPort authAuditPort;

    private AuthenticationDomainService authenticationDomainService;
    private SignInUseCaseImpl signInUseCase;

    private static final String USER_ID = "user-123";
    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "SecurePass1!";
    private static final String DEVICE_ID = "device-001";
    private static final String IP_ADDRESS = "192.168.1.1";
    private static final String USER_AGENT = "TestAgent/1.0";

    @BeforeEach
    void setUp() {
        authenticationDomainService = new AuthenticationDomainService();
        signInUseCase = new SignInUseCaseImpl(
                directoryServicePort,
                tokenProviderPort,
                refreshTokenPort,
                authAuditPort,
                authenticationDomainService);
    }

    @Test
    void signIn_withValidCredentials_returnsAuthenticationResult() {
        Credentials credentials = Credentials.of(USERNAME, PASSWORD, DEVICE_ID);
        AuthenticatedUser authenticatedUser = buildAuthenticatedUser();
        TokenPair tokenPair = buildTokenPair();
        TokenClaims tokenClaims = buildTokenClaims();

        when(directoryServicePort.authenticate(credentials)).thenReturn(Mono.just(authenticatedUser));
        when(tokenProviderPort.generateTokenPair(authenticatedUser, DEVICE_ID)).thenReturn(Mono.just(tokenPair));
        when(tokenProviderPort.parseRefreshToken(tokenPair.refreshToken())).thenReturn(Mono.just(tokenClaims));
        when(refreshTokenPort.store(eq(USER_ID), eq(DEVICE_ID), eq(tokenClaims), anyLong())).thenReturn(Mono.empty());
        when(authAuditPort.recordSignInSuccess(USER_ID, USERNAME, IP_ADDRESS, USER_AGENT)).thenReturn(Mono.empty());

        StepVerifier.create(signInUseCase.signIn(credentials, IP_ADDRESS, USER_AGENT))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.user().userId()).isEqualTo(USER_ID);
                    assertThat(result.user().username()).isEqualTo(USERNAME);
                    assertThat(result.accessToken()).isEqualTo("access-token-value");
                    assertThat(result.refreshToken()).isEqualTo("refresh-token-value");
                })
                .verifyComplete();
    }

    @Test
    void signIn_withInvalidCredentials_throwsInvalidCredentialsException() {
        Credentials credentials = Credentials.of(USERNAME, PASSWORD, DEVICE_ID);

        when(directoryServicePort.authenticate(credentials))
                .thenReturn(Mono.error(new InvalidCredentialsException(USERNAME)));
        when(authAuditPort.recordSignInFailure(eq(USERNAME), eq(IP_ADDRESS), eq(USER_AGENT), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(signInUseCase.signIn(credentials, IP_ADDRESS, USER_AGENT))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    private AuthenticatedUser buildAuthenticatedUser() {
        return AuthenticatedUser.builder()
                .userId(USER_ID)
                .username(USERNAME)
                .email(Email.of("john.doe@company.com"))
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of("ROLE_USER"))
                .groups(Set.of("developers"))
                .enabled(true)
                .build();
    }

    private TokenPair buildTokenPair() {
        Instant now = Instant.now();
        return TokenPair.builder()
                .accessToken("access-token-value")
                .refreshToken("refresh-token-value")
                .accessTokenExpiresAt(now.plusSeconds(900))
                .refreshTokenExpiresAt(now.plusSeconds(86400))
                .tokenType("Bearer")
                .build();
    }

    private TokenClaims buildTokenClaims() {
        Instant now = Instant.now();
        return TokenClaims.builder()
                .jti("jti-1234567890123456")
                .subject(USERNAME)
                .userId(USER_ID)
                .username(USERNAME)
                .email("john.doe@company.com")
                .roles(Set.of("ROLE_USER"))
                .deviceId(DEVICE_ID)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(86400))
                .issuer("security-service")
                .build();
    }
}
