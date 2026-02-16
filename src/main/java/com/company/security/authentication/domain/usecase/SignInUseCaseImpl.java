package com.company.security.authentication.domain.usecase;

import com.company.security.authentication.domain.model.AuthenticationResult;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.authentication.domain.service.AuthenticationDomainService;
import com.company.security.authentication.domain.port.input.SignInUseCase;
import com.company.security.authentication.domain.port.output.AuthAuditPort;
import com.company.security.authentication.domain.port.output.DirectoryServicePort;
import com.company.security.authentication.domain.port.output.RefreshTokenPort;
import com.company.security.authentication.domain.port.output.TokenProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Implementation of the sign-in use case.
 * Handles user authentication against the directory service.
 */
public class SignInUseCaseImpl implements SignInUseCase {

    private static final Logger log = LoggerFactory.getLogger(SignInUseCaseImpl.class);

    private final DirectoryServicePort directoryServicePort;
    private final TokenProviderPort tokenProviderPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AuthAuditPort authAuditPort;
    private final AuthenticationDomainService authenticationDomainService;

    public SignInUseCaseImpl(
            DirectoryServicePort directoryServicePort,
            TokenProviderPort tokenProviderPort,
            RefreshTokenPort refreshTokenPort,
            AuthAuditPort authAuditPort,
            AuthenticationDomainService authenticationDomainService) {
        this.directoryServicePort = directoryServicePort;
        this.tokenProviderPort = tokenProviderPort;
        this.refreshTokenPort = refreshTokenPort;
        this.authAuditPort = authAuditPort;
        this.authenticationDomainService = authenticationDomainService;
    }

    @Override
    public Mono<AuthenticationResult> signIn(Credentials credentials, String ipAddress, String userAgent) {
        log.debug("Attempting sign-in for user: {}", credentials.username());

        return directoryServicePort.authenticate(credentials)
                .doOnNext(user -> authenticationDomainService.validateUserCanSignIn(user, credentials))
                .flatMap(user -> tokenProviderPort.generateTokenPair(user, credentials.deviceId())
                        .flatMap(tokenPair ->
                                tokenProviderPort.parseRefreshToken(tokenPair.refreshToken())
                                        .flatMap(claims -> {
                                            long expirationSeconds = claims.remainingTimeInSeconds();
                                            return refreshTokenPort.store(
                                                            user.userId(),
                                                            credentials.deviceId(),
                                                            claims,
                                                            expirationSeconds)
                                                    .then(Mono.just(tokenPair));
                                        }))
                        .map(tokenPair -> AuthenticationResult.of(user, tokenPair))
                        .doOnNext(result -> recordAuditSuccess(
                                user.userId(), user.username(), ipAddress, userAgent)))
                .doOnSuccess(result -> log.info("Sign-in successful for user: {}", credentials.username()))
                .doOnError(e -> recordAuditFailure(credentials.username(), ipAddress, userAgent, e));
    }

    private void recordAuditSuccess(String userId, String username, String ipAddress, String userAgent) {
        authAuditPort.recordSignInSuccess(userId, username, ipAddress, userAgent)
                .subscribe(
                        null,
                        error -> log.warn("Failed to record sign-in success audit for user: {}", username, error)
                );
    }

    private void recordAuditFailure(String username, String ipAddress, String userAgent, Throwable cause) {
        log.warn("Sign-in failed for user: {} - {}", username, cause.getMessage());
        authAuditPort.recordSignInFailure(username, ipAddress, userAgent, cause.getMessage())
                .subscribe(
                        null,
                        error -> log.warn("Failed to record sign-in failure audit for user: {}", username, error)
                );
    }
}
