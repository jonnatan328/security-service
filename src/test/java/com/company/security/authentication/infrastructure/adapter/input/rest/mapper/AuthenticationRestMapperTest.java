package com.company.security.authentication.infrastructure.adapter.input.rest.mapper;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.AuthenticationResult;
import com.company.security.authentication.domain.model.Credentials;
import com.company.security.authentication.domain.model.TokenPair;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.request.SignInRequest;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.AuthenticationResponse;
import com.company.security.authentication.infrastructure.adapter.input.rest.dto.response.TokenResponse;
import com.company.security.shared.domain.model.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationRestMapperTest {

    private AuthenticationRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AuthenticationRestMapper();
    }

    @Test
    void toCredentials_mapsCorrectly() {
        SignInRequest request = new SignInRequest("john.doe", "password123");
        Credentials credentials = mapper.toCredentials(request, "device-001");

        assertThat(credentials.username()).isEqualTo("john.doe");
        assertThat(credentials.password()).isEqualTo("password123");
        assertThat(credentials.deviceId()).isEqualTo("device-001");
    }

    @Test
    void toAuthenticationResponse_mapsCorrectly() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .build();

        TokenPair tokenPair = TokenPair.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .accessTokenExpiresAt(Instant.now().plusSeconds(900))
                .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                .tokenType("Bearer")
                .build();

        AuthenticationResult result = AuthenticationResult.of(user, tokenPair);
        AuthenticationResponse response = mapper.toAuthenticationResponse(result);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.user().userId()).isEqualTo("user-123");
        assertThat(response.user().username()).isEqualTo("john.doe");
        assertThat(response.user().email()).isEqualTo("john@company.com");
        assertThat(response.user().firstName()).isEqualTo("John");
        assertThat(response.user().lastName()).isEqualTo("Doe");
        assertThat(response.user().roles()).contains("ROLE_USER");
    }

    @Test
    void toTokenResponse_mapsCorrectly() {
        TokenPair tokenPair = TokenPair.builder()
                .accessToken("new-access")
                .refreshToken("new-refresh")
                .accessTokenExpiresAt(Instant.now().plusSeconds(900))
                .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                .tokenType("Bearer")
                .build();

        TokenResponse response = mapper.toTokenResponse(tokenPair);

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void toUserInfo_mapsCorrectly() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .build();

        AuthenticationResponse.UserInfo userInfo = mapper.toUserInfo(user);

        assertThat(userInfo.userId()).isEqualTo("user-123");
        assertThat(userInfo.username()).isEqualTo("john.doe");
    }
}
