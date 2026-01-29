package com.company.security.token.infrastructure.application.usecase;

import com.company.security.token.domain.model.TokenStatus;
import com.company.security.token.domain.model.TokenValidationResult;
import com.company.security.token.infrastructure.application.port.input.ValidateTokenUseCase;
import com.company.security.token.infrastructure.application.port.output.TokenBlacklistCheckPort;
import com.company.security.token.infrastructure.application.port.output.TokenIntrospectionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ValidateTokenUseCaseImpl implements ValidateTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(ValidateTokenUseCaseImpl.class);

    private final TokenIntrospectionPort tokenIntrospectionPort;
    private final TokenBlacklistCheckPort tokenBlacklistCheckPort;

    public ValidateTokenUseCaseImpl(
            TokenIntrospectionPort tokenIntrospectionPort,
            TokenBlacklistCheckPort tokenBlacklistCheckPort) {
        this.tokenIntrospectionPort = tokenIntrospectionPort;
        this.tokenBlacklistCheckPort = tokenBlacklistCheckPort;
    }

    @Override
    public Mono<TokenValidationResult> validate(String rawToken) {
        log.debug("Validating token");

        return tokenIntrospectionPort.introspect(rawToken)
                .flatMap(token -> {
                    if (token.isExpired()) {
                        return Mono.just(TokenValidationResult.expired());
                    }

                    return tokenBlacklistCheckPort.isBlacklisted(token.jti())
                            .map(isBlacklisted -> {
                                if (isBlacklisted) {
                                    return TokenValidationResult.revoked();
                                }
                                return TokenValidationResult.valid(token);
                            });
                })
                .onErrorResume(e -> {
                    log.warn("Token validation failed: {}", e.getMessage());
                    return Mono.just(TokenValidationResult.invalid(
                            TokenStatus.INVALID, e.getMessage()));
                });
    }
}
