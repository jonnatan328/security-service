package com.company.security.authentication.domain.usecase;

import com.company.security.authentication.domain.port.input.SignOutUseCase;
import com.company.security.authentication.domain.port.output.AuthAuditPort;
import com.company.security.authentication.domain.port.output.RefreshTokenPort;
import com.company.security.authentication.domain.port.output.TokenBlacklistPort;
import com.company.security.authentication.domain.port.output.TokenProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Implementation of the sign-out use case.
 * Handles user sign-out by invalidating tokens.
 */
public class SignOutUseCaseImpl implements SignOutUseCase {

    private static final Logger log = LoggerFactory.getLogger(SignOutUseCaseImpl.class);

    private final TokenProviderPort tokenProviderPort;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AuthAuditPort authAuditPort;

    public SignOutUseCaseImpl(
            TokenProviderPort tokenProviderPort,
            TokenBlacklistPort tokenBlacklistPort,
            RefreshTokenPort refreshTokenPort,
            AuthAuditPort authAuditPort) {
        this.tokenProviderPort = tokenProviderPort;
        this.tokenBlacklistPort = tokenBlacklistPort;
        this.refreshTokenPort = refreshTokenPort;
        this.authAuditPort = authAuditPort;
    }

    @Override
    public Mono<Void> signOut(String accessToken, String refreshToken, String ipAddress, String userAgent) {
        log.debug("Processing sign-out request");

        return tokenProviderPort.parseAccessToken(accessToken)
                .flatMap(claims -> {
                    // Blacklist the access token
                    Mono<Void> blacklistAccess = tokenBlacklistPort.blacklist(
                            claims.jti(),
                            claims.remainingTimeInSeconds());

                    // Delete the refresh token if provided
                    Mono<Void> deleteRefresh = refreshToken != null && !refreshToken.isBlank()
                            ? refreshTokenPort.delete(claims.userId(), claims.deviceId())
                            : Mono.empty();

                    // Record the sign-out event
                    Mono<Void> recordAudit = authAuditPort.recordSignOut(
                            claims.userId(),
                            claims.username(),
                            ipAddress,
                            userAgent);

                    return Mono.when(blacklistAccess, deleteRefresh, recordAudit)
                            .doOnSuccess(v -> log.info("Sign-out successful for user: {}", claims.username()));
                })
                .onErrorResume(e -> {
                    log.error("Sign-out failed: {}", e.getMessage());
                    return Mono.error(e);
                });
    }
}
