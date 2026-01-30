package com.company.security.token.infrastructure.adapter.input.rest.handler;

import com.company.security.token.infrastructure.adapter.input.rest.dto.request.ValidateTokenRequest;
import com.company.security.token.infrastructure.adapter.input.rest.mapper.TokenRestMapper;
import com.company.security.token.domain.port.input.ValidateTokenUseCase;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class TokenValidationHandler {

    private final ValidateTokenUseCase validateTokenUseCase;
    private final TokenRestMapper mapper;

    public TokenValidationHandler(ValidateTokenUseCase validateTokenUseCase, TokenRestMapper mapper) {
        this.validateTokenUseCase = validateTokenUseCase;
        this.mapper = mapper;
    }

    public Mono<ServerResponse> validate(ServerRequest request) {
        return request.bodyToMono(ValidateTokenRequest.class)
                .flatMap(req -> validateTokenUseCase.validate(req.token()))
                .map(mapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }
}
