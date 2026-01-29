package com.company.security.authentication.infrastructure.application.usecase;

import com.company.security.authentication.domain.exception.InvalidCredentialsException;
import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.TokenPair;
import com.company.security.authentication.domain.service.AuthenticationDomainService;
import com.company.security.authentication.infrastructure.application.port.input.RefreshTokenUseCase;
import com.company.security.authentication.infrastructure.application.port.output.AuthAuditPort;
import com.company.security.authentication.infrastructure.application.port.output.DirectoryServicePort;
import com.company.security.authentication.infrastructure.application.port.output.RefreshTokenPort;
import com.company.security.authentication.infrastructure.application.port.output.TokenBlacklistPort;
import com.company.security.authentication.infrastructure.application.port.output.TokenProviderPort;
import com.company.security.shared.domain.exception.ErrorCode;
import com.company.security.token.domain.exception.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the refresh token use case.
 * Handles token refresh operations.
 */
@Service
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenUseCaseImpl.class);

    private final TokenProviderPort tokenProviderPort;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final RefreshTokenPort refreshTokenPort;
    private final DirectoryServicePort directoryServicePort;
    private final AuthAuditPort authAuditPort;
    private final AuthenticationDomainService authenticationDomainService;

    public RefreshTokenUseCaseImpl(
            TokenProviderPort tokenProviderPort,
            TokenBlacklistPort tokenBlacklistPort,
            RefreshTokenPort refreshTokenPort,
            DirectoryServicePort directoryServicePort,
            AuthAuditPort authAuditPort,
            AuthenticationDomainService authenticationDomainService) {
        this.tokenProviderPort = tokenProviderPort;
        this.tokenBlacklistPort = tokenBlacklistPort;
        this.refreshTokenPort = refreshTokenPort;
        this.directoryServicePort = directoryServicePort;
        this.authAuditPort = authAuditPort;
        this.authenticationDomainService = authenticationDomainService;
    }

    @Override
    public Mono<TokenPair> refreshToken(String refreshToken, String ipAddress, String userAgent) {
        log.debug("Processing token refresh request");

        return tokenProviderPort.parseRefreshToken(refreshToken)
                .flatMap(claims -> {
                    // Validate refresh request
                    if (!authenticationDomainService.isValidRefreshRequest(claims.userId(), claims.deviceId())) {
                        return Mono.error(new InvalidTokenException("Invalid refresh token claims"));
                    }

                    // Check if the refresh token is blacklisted
                    return tokenBlacklistPort.isBlacklisted(claims.jti())
                            .flatMap(isBlacklisted -> {
                                if (isBlacklisted) {
                                    log.warn("Refresh token is blacklisted: {}", claims.jti());
                                    return Mono.error(new InvalidTokenException("Refresh token has been revoked"));
                                }

                                // Verify the stored refresh token matches
                                return refreshTokenPort.retrieve(claims.userId(), claims.deviceId())
                                        .switchIfEmpty(Mono.error(new InvalidTokenException("Refresh token not found")))
                                        .flatMap(storedClaims -> {
                                            if (!storedClaims.jti().equals(claims.jti())) {
                                                log.warn("Refresh token mismatch for user: {}", claims.userId());
                                                return Mono.error(new InvalidTokenException("Invalid refresh token"));
                                            }

                                            // Lookup user to get current roles/permissions
                                            return directoryServicePort.findByUsername(claims.username())
                                                    .switchIfEmpty(Mono.error(new InvalidCredentialsException(claims.username())))
                                                    .flatMap(user -> generateNewTokens(user, claims.deviceId(), claims.jti()))
                                                    .flatMap(newTokenPair -> authAuditPort.recordTokenRefresh(
                                                                    claims.userId(),
                                                                    claims.username(),
                                                                    ipAddress,
                                                                    userAgent)
                                                            .thenReturn(newTokenPair));
                                        });
                            });
                })
                .doOnSuccess(tokenPair -> log.info("Token refresh successful"))
                .onErrorResume(e -> {
                    log.error("Token refresh failed: {}", e.getMessage());
                    return Mono.error(e);
                });
    }

    private Mono<TokenPair> generateNewTokens(AuthenticatedUser user, String deviceId, String oldRefreshJti) {
        return tokenProviderPort.generateTokenPair(user, deviceId)
                .flatMap(newTokenPair -> {
                    // Blacklist the old refresh token
                    Mono<Void> blacklistOld = tokenBlacklistPort.blacklist(oldRefreshJti, 60);

                    // Store the new refresh token
                    Mono<Void> storeNew = tokenProviderPort.parseRefreshToken(newTokenPair.refreshToken())
                            .flatMap(newClaims -> refreshTokenPort.store(
                                    user.userId(),
                                    deviceId,
                                    newClaims,
                                    newClaims.remainingTimeInSeconds()));

                    return Mono.when(blacklistOld, storeNew)
                            .thenReturn(newTokenPair);
                });
    }
}
