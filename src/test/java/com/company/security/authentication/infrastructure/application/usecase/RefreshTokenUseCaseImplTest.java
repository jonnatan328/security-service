package com.company.security.authentication.infrastructure.application.usecase;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.TokenClaims;
import com.company.security.authentication.domain.model.TokenPair;
import com.company.security.authentication.domain.service.AuthenticationDomainService;
import com.company.security.authentication.infrastructure.application.port.output.AuthAuditPort;
import com.company.security.authentication.infrastructure.application.port.output.DirectoryServicePort;
import com.company.security.authentication.infrastructure.application.port.output.RefreshTokenPort;
import com.company.security.authentication.infrastructure.application.port.output.TokenBlacklistPort;
import com.company.security.authentication.infrastructure.application.port.output.TokenProviderPort;
import com.company.security.shared.domain.model.Email;
import com.company.security.token.domain.exception.InvalidTokenException;
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
class RefreshTokenUseCaseImplTest {

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private TokenBlacklistPort tokenBlacklistPort;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @Mock
    private DirectoryServicePort directoryServicePort;

    @Mock
    private AuthAuditPort authAuditPort;

    private AuthenticationDomainService authenticationDomainService;
    private RefreshTokenUseCaseImpl refreshTokenUseCase;

    private static final String REFRESH_TOKEN = "refresh-token-value";
    private static final String USER_ID = "user-123";
    private static final String USERNAME = "john.doe";
    private static final String DEVICE_ID = "device-001";
    private static final String IP_ADDRESS = "192.168.1.1";
    private static final String USER_AGENT = "TestAgent/1.0";
    private static final String JTI = "jti-1234567890123456";

    @BeforeEach
    void setUp() {
        authenticationDomainService = new AuthenticationDomainService();
        refreshTokenUseCase = new RefreshTokenUseCaseImpl(
                tokenProviderPort,
                tokenBlacklistPort,
                refreshTokenPort,
                directoryServicePort,
                authAuditPort,
                authenticationDomainService);
    }

    @Test
    void refreshToken_withValidRefreshToken_returnsNewTokenPair() {
        TokenClaims claims = buildTokenClaims();
        TokenClaims storedClaims = buildTokenClaims();
        AuthenticatedUser user = buildAuthenticatedUser();
        TokenPair newTokenPair = buildTokenPair("new-access-token", "new-refresh-token");
        TokenClaims newRefreshClaims = buildTokenClaims("new-jti-1234567890123456");

        when(tokenProviderPort.parseRefreshToken(REFRESH_TOKEN)).thenReturn(Mono.just(claims));
        when(tokenBlacklistPort.isBlacklisted(JTI)).thenReturn(Mono.just(false));
        when(refreshTokenPort.retrieve(USER_ID, DEVICE_ID)).thenReturn(Mono.just(storedClaims));
        when(directoryServicePort.findByUsername(USERNAME)).thenReturn(Mono.just(user));
        when(tokenProviderPort.generateTokenPair(user, DEVICE_ID)).thenReturn(Mono.just(newTokenPair));
        when(tokenBlacklistPort.blacklist(eq(JTI), anyLong())).thenReturn(Mono.empty());
        when(tokenProviderPort.parseRefreshToken("new-refresh-token")).thenReturn(Mono.just(newRefreshClaims));
        when(refreshTokenPort.store(eq(USER_ID), eq(DEVICE_ID), eq(newRefreshClaims), anyLong())).thenReturn(Mono.empty());
        when(authAuditPort.recordTokenRefresh(USER_ID, USERNAME, IP_ADDRESS, USER_AGENT)).thenReturn(Mono.empty());

        StepVerifier.create(refreshTokenUseCase.refreshToken(REFRESH_TOKEN, IP_ADDRESS, USER_AGENT))
                .assertNext(tokenPair -> {
                    assertThat(tokenPair.accessToken()).isEqualTo("new-access-token");
                    assertThat(tokenPair.refreshToken()).isEqualTo("new-refresh-token");
                })
                .verifyComplete();
    }

    @Test
    void refreshToken_withBlacklistedToken_throwsInvalidTokenException() {
        TokenClaims claims = buildTokenClaims();

        when(tokenProviderPort.parseRefreshToken(REFRESH_TOKEN)).thenReturn(Mono.just(claims));
        when(tokenBlacklistPort.isBlacklisted(JTI)).thenReturn(Mono.just(true));

        StepVerifier.create(refreshTokenUseCase.refreshToken(REFRESH_TOKEN, IP_ADDRESS, USER_AGENT))
                .expectError(InvalidTokenException.class)
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

    private TokenPair buildTokenPair(String accessToken, String refreshToken) {
        Instant now = Instant.now();
        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(now.plusSeconds(900))
                .refreshTokenExpiresAt(now.plusSeconds(86400))
                .tokenType("Bearer")
                .build();
    }

    private TokenClaims buildTokenClaims() {
        return buildTokenClaims(JTI);
    }

    private TokenClaims buildTokenClaims(String jti) {
        Instant now = Instant.now();
        return TokenClaims.builder()
                .jti(jti)
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
