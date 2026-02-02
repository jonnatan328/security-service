package com.company.security.authentication.infrastructure.adapter.input.rest.controller;

import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.RefreshTokenRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignInRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignOutRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.AuthenticationResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.TokenResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.handler.AuthenticationHandler;
import com.company.security.shared.infrastructure.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for sign-in, sign-out, and token refresh")
public class AuthenticationController {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_DEVICE_ID = "X-Device-Id";

    private final AuthenticationHandler handler;

    public AuthenticationController(AuthenticationHandler handler) {
        this.handler = handler;
    }

    @PostMapping(value = "/signin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "signIn",
            summary = "User sign-in",
            description = "Authenticates a user against the directory service and returns access/refresh tokens",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = SignInRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful",
                            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "423", description = "Account locked",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public Mono<ResponseEntity<AuthenticationResponse>> signIn(
            @Valid @RequestBody SignInRequest request,
            @RequestHeader(value = X_DEVICE_ID, required = false) String deviceId,
            ServerHttpRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeaders().getFirst(HttpHeaders.USER_AGENT);

        return handler.signIn(request, deviceId, ipAddress, userAgent)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/signout", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "signOut",
            summary = "User sign-out",
            description = "Signs out the user by invalidating access and refresh tokens",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Sign-out successful"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public Mono<ResponseEntity<Void>> signOut(
            @RequestBody(required = false) SignOutRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            ServerHttpRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeaders().getFirst(HttpHeaders.USER_AGENT);
        String accessToken = extractBearerToken(authorization);
        String refreshToken = request != null ? request.refreshToken() : null;

        return handler.signOut(accessToken, refreshToken, ipAddress, userAgent)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "refreshToken",
            summary = "Refresh access token",
            description = "Exchanges a valid refresh token for a new access/refresh token pair",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refresh successful",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    public Mono<ResponseEntity<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            ServerHttpRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeaders().getFirst(HttpHeaders.USER_AGENT);

        return handler.refresh(request, ipAddress, userAgent)
                .map(ResponseEntity::ok);
    }

    private String extractIpAddress(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getFirst(X_FORWARDED_FOR))
                .map(header -> header.split(",")[0].trim())
                .orElseGet(() -> {
                    var remoteAddress = request.getRemoteAddress();
                    return remoteAddress != null
                            ? remoteAddress.getAddress().getHostAddress()
                            : "unknown";
                });
    }

    private String extractBearerToken(String authorization) {
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
