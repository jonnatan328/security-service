package com.company.security.token.infrastructure.adapter.input.rest.controller;

import com.company.security.token.infrastructure.adapter.input.rest.dto.request.ValidateTokenRequest;
import com.company.security.token.infrastructure.adapter.input.rest.dto.response.TokenValidationResponse;
import com.company.security.token.infrastructure.adapter.input.rest.handler.TokenValidationHandler;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/v1/token")
public class TokenValidationController {

    private final TokenValidationHandler handler;

    public TokenValidationController(TokenValidationHandler handler) {
        this.handler = handler;
    }

    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<TokenValidationResponse>> validate(
            @Valid @RequestBody ValidateTokenRequest request) {
        return handler.validate(request)
                .map(ResponseEntity::ok);
    }
}
