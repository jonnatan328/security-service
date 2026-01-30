package com.company.security.token.domain.usecase;

import com.company.security.token.domain.model.Token;
import com.company.security.token.domain.model.TokenStatus;
import com.company.security.token.domain.port.output.TokenBlacklistCheckPort;
import com.company.security.token.domain.port.output.TokenIntrospectionPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateTokenUseCaseImplTest {

    @Mock
    private TokenIntrospectionPort tokenIntrospectionPort;

    @Mock
    private TokenBlacklistCheckPort tokenBlacklistCheckPort;

    @InjectMocks
    private ValidateTokenUseCaseImpl validateTokenUseCase;

    private static final String RAW_TOKEN = "raw-token-value";
    private static final String JTI = "jti-1234567890123456";
    private static final String USER_ID = "user-123";
    private static final String USERNAME = "john.doe";

    @Test
    void validate_withValidToken_returnsValid() {
        Token token = buildValidToken();

        when(tokenIntrospectionPort.introspect(RAW_TOKEN)).thenReturn(Mono.just(token));
        when(tokenBlacklistCheckPort.isBlacklisted(JTI)).thenReturn(Mono.just(false));

        StepVerifier.create(validateTokenUseCase.validate(RAW_TOKEN))
                .assertNext(result -> {
                    assertThat(result.valid()).isTrue();
                    assertThat(result.status()).isEqualTo(TokenStatus.VALID);
                    assertThat(result.userId()).isEqualTo(USER_ID);
                })
                .verifyComplete();
    }

    @Test
    void validate_withBlacklistedToken_returnsRevoked() {
        Token token = buildValidToken();

        when(tokenIntrospectionPort.introspect(RAW_TOKEN)).thenReturn(Mono.just(token));
        when(tokenBlacklistCheckPort.isBlacklisted(JTI)).thenReturn(Mono.just(true));

        StepVerifier.create(validateTokenUseCase.validate(RAW_TOKEN))
                .assertNext(result -> {
                    assertThat(result.valid()).isFalse();
                    assertThat(result.status()).isEqualTo(TokenStatus.REVOKED);
                })
                .verifyComplete();
    }

    @Test
    void validate_withExpiredToken_returnsExpired() {
        Token expiredToken = buildExpiredToken();

        when(tokenIntrospectionPort.introspect(RAW_TOKEN)).thenReturn(Mono.just(expiredToken));

        StepVerifier.create(validateTokenUseCase.validate(RAW_TOKEN))
                .assertNext(result -> {
                    assertThat(result.valid()).isFalse();
                    assertThat(result.status()).isEqualTo(TokenStatus.EXPIRED);
                })
                .verifyComplete();
    }

    private Token buildValidToken() {
        Instant now = Instant.now();
        return Token.builder()
                .rawToken(RAW_TOKEN)
                .jti(JTI)
                .subject(USERNAME)
                .userId(USER_ID)
                .email("john.doe@company.com")
                .roles(Set.of("ROLE_USER"))
                .issuedAt(now.minusSeconds(60))
                .expiresAt(now.plusSeconds(840))
                .issuer("security-service")
                .build();
    }

    private Token buildExpiredToken() {
        Instant now = Instant.now();
        return Token.builder()
                .rawToken(RAW_TOKEN)
                .jti(JTI)
                .subject(USERNAME)
                .userId(USER_ID)
                .email("john.doe@company.com")
                .roles(Set.of("ROLE_USER"))
                .issuedAt(now.minusSeconds(1800))
                .expiresAt(now.minusSeconds(900))
                .issuer("security-service")
                .build();
    }
}
