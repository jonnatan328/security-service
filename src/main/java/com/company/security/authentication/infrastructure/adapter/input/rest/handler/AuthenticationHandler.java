package com.company.security.authentication.infrastructure.adapter.input.rest.handler;

import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.RefreshTokenRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignInRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignOutRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.AuthenticationResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.TokenResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.mapper.AuthenticationRestMapper;
import com.company.security.authentication.infrastructure.application.port.input.RefreshTokenUseCase;
import com.company.security.authentication.infrastructure.application.port.input.SignInUseCase;
import com.company.security.authentication.infrastructure.application.port.input.SignOutUseCase;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Handler for authentication REST endpoints.
 */
@Component
public class AuthenticationHandler {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final SignInUseCase signInUseCase;
    private final SignOutUseCase signOutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final AuthenticationRestMapper mapper;
    private final Validator validator;

    public AuthenticationHandler(
            SignInUseCase signInUseCase,
            SignOutUseCase signOutUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            AuthenticationRestMapper mapper,
            Validator validator) {
        this.signInUseCase = signInUseCase;
        this.signOutUseCase = signOutUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.mapper = mapper;
        this.validator = validator;
    }

    public Mono<ServerResponse> signIn(ServerRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = extractUserAgent(request);

        return request.bodyToMono(SignInRequest.class)
                .flatMap(this::validate)
                .map(mapper::toCredentials)
                .flatMap(credentials -> signInUseCase.signIn(credentials, ipAddress, userAgent))
                .map(mapper::toAuthenticationResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> signOut(ServerRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = extractUserAgent(request);
        String accessToken = extractBearerToken(request);

        return request.bodyToMono(SignOutRequest.class)
                .defaultIfEmpty(new SignOutRequest(null))
                .flatMap(signOutRequest -> signOutUseCase.signOut(
                        accessToken,
                        signOutRequest.refreshToken(),
                        ipAddress,
                        userAgent))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> refresh(ServerRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = extractUserAgent(request);

        return request.bodyToMono(RefreshTokenRequest.class)
                .flatMap(this::validate)
                .flatMap(refreshRequest -> refreshTokenUseCase.refreshToken(
                        refreshRequest.refreshToken(),
                        ipAddress,
                        userAgent))
                .map(mapper::toTokenResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }

    private String extractIpAddress(ServerRequest request) {
        return Optional.ofNullable(request.headers().firstHeader(X_FORWARDED_FOR))
                .map(header -> header.split(",")[0].trim())
                .orElseGet(() -> request.remoteAddress()
                        .map(address -> address.getAddress().getHostAddress())
                        .orElse("unknown"));
    }

    private String extractUserAgent(ServerRequest request) {
        return request.headers().firstHeader(HttpHeaders.USER_AGENT);
    }

    private String extractBearerToken(ServerRequest request) {
        String authHeader = request.headers().firstHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private <T> Mono<T> validate(T object) {
        Errors errors = new BeanPropertyBindingResult(object, object.getClass().getSimpleName());
        validator.validate(object).forEach(violation ->
            errors.rejectValue(
                violation.getPropertyPath().toString(),
                "",
                violation.getMessage()
            )
        );
        if (errors.hasErrors()) {
            return Mono.error(new ServerWebInputException(errors.getAllErrors().toString()));
        }
        return Mono.just(object);
    }
}
