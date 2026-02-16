package com.company.security.authentication.domain.model;

import com.company.security.shared.domain.model.Email;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationResultTest {

    @Test
    void of_withValidInputs_createsResult() {
        AuthenticatedUser user = buildUser();
        TokenPair tokenPair = buildTokenPair();

        AuthenticationResult result = AuthenticationResult.of(user, tokenPair);

        assertThat(result.user()).isEqualTo(user);
        assertThat(result.tokenPair()).isEqualTo(tokenPair);
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void of_withNullUser_throwsException() {
        TokenPair tokenPair = buildTokenPair();
        assertThatThrownBy(() -> AuthenticationResult.of(null, tokenPair))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void of_withNullTokenPair_throwsException() {
        AuthenticatedUser user = buildUser();
        assertThatThrownBy(() -> AuthenticationResult.of(user, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equals_withSameValues_returnsTrue() {
        AuthenticatedUser user = buildUser();
        TokenPair tokenPair = buildTokenPair();

        AuthenticationResult result1 = AuthenticationResult.of(user, tokenPair);
        AuthenticationResult result2 = AuthenticationResult.of(user, tokenPair);

        assertThat(result1).isEqualTo(result2).hasSameHashCodeAs(result2);
    }

    @Test
    void equals_withSameInstance_returnsTrue() {
        AuthenticationResult result = AuthenticationResult.of(buildUser(), buildTokenPair());
        assertThat(result).isEqualTo(result);
    }

    @Test
    void equals_withNull_returnsFalse() {
        AuthenticationResult result = AuthenticationResult.of(buildUser(), buildTokenPair());
        assertThat(result).isNotEqualTo(null);
    }

    @Test
    void toString_containsRelevantInfo() {
        AuthenticationResult result = AuthenticationResult.of(buildUser(), buildTokenPair());
        assertThat(result.toString()).contains("AuthenticationResult");
    }

    private AuthenticatedUser buildUser() {
        return AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .build();
    }

    private TokenPair buildTokenPair() {
        return TokenPair.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .accessTokenExpiresAt(Instant.now().plusSeconds(900))
                .refreshTokenExpiresAt(Instant.now().plusSeconds(86400))
                .tokenType("Bearer")
                .build();
    }
}
