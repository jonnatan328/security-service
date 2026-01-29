package com.company.security.token.infrastructure.application.port.input;

import com.company.security.token.domain.model.TokenValidationResult;
import reactor.core.publisher.Mono;

public interface ValidateTokenUseCase {

    Mono<TokenValidationResult> validate(String token);
}
