package com.company.security.authentication.infrastructure.adapter.input.rest.handler;

import com.company.security.authentication.domain.port.input.RefreshTokenUseCase;
import com.company.security.authentication.domain.port.input.SignInUseCase;
import com.company.security.authentication.domain.port.input.SignOutUseCase;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.RefreshTokenRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignInRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.AuthenticationResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.TokenResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.mapper.AuthenticationRestMapper;
import reactor.core.publisher.Mono;

/**
 * Handler for authentication REST endpoints.
 * Orchestrates use case calls and response mapping.
 */
public class AuthenticationHandler {

    private final SignInUseCase signInUseCase;
    private final SignOutUseCase signOutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final AuthenticationRestMapper mapper;

    public AuthenticationHandler(
            SignInUseCase signInUseCase,
            SignOutUseCase signOutUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            AuthenticationRestMapper mapper) {
        this.signInUseCase = signInUseCase;
        this.signOutUseCase = signOutUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.mapper = mapper;
    }

    public Mono<AuthenticationResponse> signIn(SignInRequest request, String deviceId,
                                                  String ipAddress, String userAgent) {
        return signInUseCase.signIn(mapper.toCredentials(request, deviceId), ipAddress, userAgent)
                .map(mapper::toAuthenticationResponse);
    }

    public Mono<Void> signOut(String accessToken, String refreshToken, String ipAddress, String userAgent) {
        return signOutUseCase.signOut(accessToken, refreshToken, ipAddress, userAgent);
    }

    public Mono<TokenResponse> refresh(RefreshTokenRequest request, String ipAddress, String userAgent) {
        return refreshTokenUseCase.refreshToken(request.refreshToken(), ipAddress, userAgent)
                .map(mapper::toTokenResponse);
    }
}
