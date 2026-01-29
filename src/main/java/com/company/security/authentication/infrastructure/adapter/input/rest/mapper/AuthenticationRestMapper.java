package com.company.security.authentication.infrastructure.adapter.input.rest.mapper;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.AuthenticationResult;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.authentication.domain.model.TokenPair;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignInRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.AuthenticationResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.TokenResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for authentication REST layer.
 */
@Component
public class AuthenticationRestMapper {

    public Credentials toCredentials(SignInRequest request) {
        return Credentials.of(request.username(), request.password(), request.deviceId());
    }

    public AuthenticationResponse toAuthenticationResponse(AuthenticationResult result) {
        TokenPair tokenPair = result.tokenPair();
        AuthenticatedUser user = result.user();

        return new AuthenticationResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.tokenType(),
                tokenPair.accessTokenExpiresInSeconds(),
                toUserInfo(user)
        );
    }

    public AuthenticationResponse.UserInfo toUserInfo(AuthenticatedUser user) {
        return new AuthenticationResponse.UserInfo(
                user.userId(),
                user.username(),
                user.email().value(),
                user.firstName(),
                user.lastName(),
                user.roles()
        );
    }

    public TokenResponse toTokenResponse(TokenPair tokenPair) {
        return new TokenResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.tokenType(),
                tokenPair.accessTokenExpiresInSeconds()
        );
    }
}
