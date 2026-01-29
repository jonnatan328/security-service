package com.company.security.token.infrastructure.adapter.input.rest.mapper;

import com.company.security.token.domain.model.TokenValidationResult;
import com.company.security.token.infrastructure.adapter.input.rest.dto.response.TokenValidationResponse;
import org.springframework.stereotype.Component;

@Component
public class TokenRestMapper {

    public TokenValidationResponse toResponse(TokenValidationResult result) {
        return new TokenValidationResponse(
                result.valid(),
                result.status().name(),
                result.userId(),
                result.username(),
                result.email(),
                result.roles(),
                result.expiresAt(),
                result.errorMessage()
        );
    }
}
