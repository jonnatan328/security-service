package com.company.security.authentication.infrastructure.application.usecase;

import com.company.security.authentication.domain.exception.AccountLockedException;
import com.company.security.authentication.domain.exception.InvalidCredentialsException;
import com.company.security.authentication.domain.model.AuthenticationResult;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.authentication.domain.service.AuthenticationDomainService;
import com.company.security.authentication.infrastructure.application.port.input.SignInUseCase;
import com.company.security.authentication.infrastructure.application.port.output.AuthAuditPort;
import com.company.security.authentication.infrastructure.application.port.output.DirectoryServicePort;
import com.company.security.authentication.infrastructure.application.port.output.RefreshTokenPort;
import com.company.security.authentication.infrastructure.application.port.output.TokenProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the sign-in use case.
 * Handles user authentication against the directory service.
 */
@Service
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
                        .flatMap(tokenPair -> {
                            // Parse the refresh token to get claims for storage
                            return tokenProviderPort.parseRefreshToken(tokenPair.refreshToken())
                                    .flatMap(claims -> {
                                        long expirationSeconds = claims.remainingTimeInSeconds();
                                        return refreshTokenPort.store(
                                                        user.userId(),
                                                        credentials.deviceId(),
                                                        claims,
                                                        expirationSeconds)
                                                .then(Mono.just(tokenPair));
                                    });
                        })
                        .map(tokenPair -> AuthenticationResult.of(user, tokenPair))
                        .flatMap(result -> authAuditPort.recordSignInSuccess(
                                        user.userId(),
                                        user.username(),
                                        ipAddress,
                                        userAgent)
                                .thenReturn(result)))
                .doOnSuccess(result -> log.info("Sign-in successful for user: {}", credentials.username()))
                .onErrorResume(InvalidCredentialsException.class, e -> {
                    log.warn("Sign-in failed for user: {} - Invalid credentials", credentials.username());
                    return authAuditPort.recordSignInFailure(
                                    credentials.username(),
                                    ipAddress,
                                    userAgent,
                                    "Invalid credentials")
                            .then(Mono.error(e));
                })
                .onErrorResume(AccountLockedException.class, e -> {
                    log.warn("Sign-in failed for user: {} - Account locked", credentials.username());
                    return authAuditPort.recordSignInFailure(
                                    credentials.username(),
                                    ipAddress,
                                    userAgent,
                                    "Account locked")
                            .then(Mono.error(e));
                })
                .onErrorResume(e -> {
                    if (!(e instanceof InvalidCredentialsException) && !(e instanceof AccountLockedException)) {
                        log.error("Sign-in failed for user: {} - {}", credentials.username(), e.getMessage());
                        return authAuditPort.recordSignInFailure(
                                        credentials.username(),
                                        ipAddress,
                                        userAgent,
                                        e.getMessage())
                                .then(Mono.error(e));
                    }
                    return Mono.error(e);
                });
    }
}
