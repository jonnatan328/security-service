package com.company.security.authentication.domain.usecase;

import com.company.security.authentication.domain.model.TokenClaims;
import com.company.security.authentication.domain.port.output.AuthAuditPort;
import com.company.security.authentication.domain.port.output.RefreshTokenPort;
import com.company.security.authentication.domain.port.output.TokenBlacklistPort;
import com.company.security.authentication.domain.port.output.TokenProviderPort;
import com.company.security.token.domain.exception.InvalidTokenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignOutUseCaseImplTest {

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private TokenBlacklistPort tokenBlacklistPort;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @Mock
    private AuthAuditPort authAuditPort;

    @InjectMocks
    private SignOutUseCaseImpl signOutUseCase;

    private static final String ACCESS_TOKEN = "access-token-value";
    private static final String REFRESH_TOKEN = "refresh-token-value";
    private static final String USER_ID = "user-123";
    private static final String USERNAME = "john.doe";
    private static final String DEVICE_ID = "device-001";
    private static final String IP_ADDRESS = "192.168.1.1";
    private static final String USER_AGENT = "TestAgent/1.0";

    @Test
    void signOut_withValidToken_completesSuccessfully() {
        TokenClaims claims = buildTokenClaims();

        when(tokenProviderPort.parseAccessToken(ACCESS_TOKEN)).thenReturn(Mono.just(claims));
        when(tokenBlacklistPort.blacklist(eq(claims.jti()), anyLong())).thenReturn(Mono.empty());
        when(refreshTokenPort.delete(USER_ID, DEVICE_ID)).thenReturn(Mono.empty());
        when(authAuditPort.recordSignOut(USER_ID, USERNAME, IP_ADDRESS, USER_AGENT)).thenReturn(Mono.empty());

        StepVerifier.create(signOutUseCase.signOut(ACCESS_TOKEN, REFRESH_TOKEN, IP_ADDRESS, USER_AGENT))
                .verifyComplete();
    }

    @Test
    void signOut_withInvalidToken_throwsError() {
        when(tokenProviderPort.parseAccessToken(ACCESS_TOKEN))
                .thenReturn(Mono.error(new InvalidTokenException("Invalid access token")));

        StepVerifier.create(signOutUseCase.signOut(ACCESS_TOKEN, REFRESH_TOKEN, IP_ADDRESS, USER_AGENT))
                .expectError(InvalidTokenException.class)
                .verify();
    }

    private TokenClaims buildTokenClaims() {
        Instant now = Instant.now();
        return TokenClaims.builder()
                .jti("jti-1234567890123456")
                .subject(USER_ID)
                .userId(USER_ID)
                .username(USERNAME)
                .email("john.doe@company.com")
                .roles(Set.of("ROLE_USER"))
                .deviceId(DEVICE_ID)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .issuer("security-service")
                .build();
    }
}
