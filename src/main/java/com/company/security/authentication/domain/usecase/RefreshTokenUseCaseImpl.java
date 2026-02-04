package com.company.security.authentication.domain.usecase;

import com.company.security.authentication.domain.exception.InvalidCredentialsException;
import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.TokenClaims;
import com.company.security.authentication.domain.model.TokenPair;
import com.company.security.authentication.domain.service.AuthenticationDomainService;
import com.company.security.authentication.domain.port.input.RefreshTokenUseCase;
import com.company.security.authentication.domain.port.output.AuthAuditPort;
import com.company.security.authentication.domain.port.output.DirectoryServicePort;
import com.company.security.authentication.domain.port.output.RefreshTokenPort;
import com.company.security.authentication.domain.port.output.TokenBlacklistPort;
import com.company.security.authentication.domain.port.output.TokenProviderPort;
import com.company.security.token.domain.exception.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Implementation of the refresh token use case.
 * Handles token refresh operations.
 */
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
                    if (!authenticationDomainService.isValidRefreshRequest(claims.userId(), claims.deviceId())) {
                        return Mono.error(new InvalidTokenException("Invalid refresh token claims"));
                    }

                    return tokenBlacklistPort.isBlacklisted(claims.jti())
                            .flatMap(isBlacklisted -> {
                                if (isBlacklisted) {
                                    log.warn("Reuse of already-rotated refresh token detected for user: {}. "
                                            + "Possible token compromise — invalidating all sessions.", claims.userId());
                                    return refreshTokenPort.deleteAllForUser(claims.userId())
                                            .then(Mono.error(new InvalidTokenException("Refresh token has been revoked")));
                                }

                                return refreshTokenPort.retrieve(claims.userId(), claims.deviceId())
                                        .switchIfEmpty(Mono.error(new InvalidTokenException("Refresh token not found")))
                                        .flatMap(storedClaims -> {
                                            if (!storedClaims.jti().equals(claims.jti())) {
                                                log.warn("Refresh token mismatch for user: {}", claims.userId());
                                                return Mono.error(new InvalidTokenException("Invalid refresh token"));
                                            }

                                            return directoryServicePort.findByUsername(claims.username())
                                                    .switchIfEmpty(Mono.error(new InvalidCredentialsException(claims.username())))
                                                    .flatMap(user -> generateNewTokens(user, claims.deviceId(), claims.jti(), claims));
                                        });
                            });
                })
                .doOnNext(tokenPair -> log.info("Token refresh successful"))
                .doOnNext(tokenPair -> recordAudit(ipAddress, userAgent))
                .onErrorResume(e -> {
                    log.error("Token refresh failed: {}", e.getMessage());
                    return Mono.error(e);
                });
    }

    private Mono<TokenPair> generateNewTokens(AuthenticatedUser user, String deviceId, String oldRefreshJti, TokenClaims oldClaims) {
        return tokenProviderPort.generateTokenPair(user, deviceId)
                .flatMap(newTokenPair -> {
                    Mono<Void> blacklistOld = tokenBlacklistPort.blacklist(oldRefreshJti, oldClaims.remainingTimeInSeconds());

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

    private void recordAudit(String ipAddress, String userAgent) {
        // Token claims already validated at this point; audit is fire-and-forget
        authAuditPort.recordTokenRefresh(null, null, ipAddress, userAgent)
                .subscribe(
                        null,
                        error -> log.warn("Failed to record token refresh audit", error)
                );
    }
}
