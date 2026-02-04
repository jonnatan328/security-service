package com.company.security.token.infrastructure.adapter.input.rest.handler;

import com.company.security.token.domain.port.input.ValidateTokenUseCase;
import com.company.security.token.infrastructure.adapter.input.rest.dto.request.ValidateTokenRequest;
import com.company.security.token.infrastructure.adapter.input.rest.dto.response.TokenValidationResponse;
import com.company.security.token.infrastructure.adapter.input.rest.mapper.TokenRestMapper;
import com.company.security.shared.infrastructure.adapter.output.ratelimit.RedisRateLimited;
import reactor.core.publisher.Mono;

/**
 * Handler for token validation REST endpoints.
 * Orchestrates use case calls and response mapping.
 */
public class TokenValidationHandler {

    private final ValidateTokenUseCase validateTokenUseCase;
    private final TokenRestMapper mapper;

    public TokenValidationHandler(ValidateTokenUseCase validateTokenUseCase, TokenRestMapper mapper) {
        this.validateTokenUseCase = validateTokenUseCase;
        this.mapper = mapper;
    }

    @RedisRateLimited(keyPrefix = "security:ratelimit:tokenvalidation:", maxRequests = 100, windowSeconds = 1, keyParamName = "ipAddress")
    public Mono<TokenValidationResponse> validate(ValidateTokenRequest request, String ipAddress) {
        return validateTokenUseCase.validate(request.token())
                .map(mapper::toResponse);
    }
}
