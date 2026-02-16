package com.company.security.authentication.domain.model;

import com.company.security.shared.domain.model.Email;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedUserTest {

    @Test
    void builder_withAllFields_createsUser() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of("ROLE_USER", "ROLE_ADMIN"))
                .groups(Set.of("developers"))
                .enabled(true)
                .build();

        assertThat(user.userId()).isEqualTo("user-123");
        assertThat(user.username()).isEqualTo("john.doe");
        assertThat(user.email().value()).isEqualTo("john@company.com");
        assertThat(user.firstName()).isEqualTo("John");
        assertThat(user.lastName()).isEqualTo("Doe");
        assertThat(user.roles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(user.groups()).contains("developers");
        assertThat(user.enabled()).isTrue();
    }

    @Test
    void builder_withEmailString_createsUser() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email("john@company.com")
                .build();

        assertThat(user.email().value()).isEqualTo("john@company.com");
    }

    @Test
    void builder_withNullRoles_returnsEmptySet() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .build();

        assertThat(user.roles()).isEmpty();
        assertThat(user.groups()).isEmpty();
    }

    @Test
    void fullName_withBothNames_returnsCombined() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .firstName("John")
                .lastName("Doe")
                .build();

        assertThat(user.fullName()).isEqualTo("John Doe");
    }

    @Test
    void fullName_withOnlyFirstName_returnsFirstName() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .firstName("John")
                .build();

        assertThat(user.fullName()).isEqualTo("John");
    }

    @Test
    void fullName_withOnlyLastName_returnsLastName() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .lastName("Doe")
                .build();

        assertThat(user.fullName()).isEqualTo("Doe");
    }

    @Test
    void fullName_withNoNames_returnsUsername() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .build();

        assertThat(user.fullName()).isEqualTo("john.doe");
    }

    @Test
    void hasRole_withExistingRole_returnsTrue() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .roles(Set.of("ROLE_USER"))
                .build();

        assertThat(user.hasRole("ROLE_USER")).isTrue();
        assertThat(user.hasRole("ROLE_ADMIN")).isFalse();
    }

    @Test
    void hasAnyRole_withMatchingRole_returnsTrue() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .roles(Set.of("ROLE_USER"))
                .build();

        assertThat(user.hasAnyRole(Set.of("ROLE_USER", "ROLE_ADMIN"))).isTrue();
        assertThat(user.hasAnyRole(Set.of("ROLE_ADMIN", "ROLE_SUPER"))).isFalse();
    }

    @Test
    void equals_withSameUserId_returnsTrue() {
        AuthenticatedUser user1 = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .build();

        AuthenticatedUser user2 = AuthenticatedUser.builder()
                .userId("user-123")
                .username("jane.doe")
                .email(Email.of("jane@company.com"))
                .build();

        assertThat(user1).isEqualTo(user2).hasSameHashCodeAs(user2);
    }

    @Test
    void equals_withDifferentUserId_returnsFalse() {
        AuthenticatedUser user1 = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .build();

        AuthenticatedUser user2 = AuthenticatedUser.builder()
                .userId("user-456")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .build();

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void equals_withNull_returnsFalse() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .build();

        assertThat(user).isNotEqualTo(null);
    }

    @Test
    void equals_withSameInstance_returnsTrue() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .build();

        assertThat(user).isEqualTo(user);
    }

    @Test
    void toString_containsRelevantInfo() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe")
                .email(Email.of("john@company.com"))
                .enabled(true)
                .build();

        assertThat(user.toString()).contains("user-123", "john.doe");
    }

    @Test
    void builder_withNullUserId_throwsException() {
        Email email = Email.of("john@company.com");
        AuthenticatedUser.Builder builder = AuthenticatedUser.builder()
                .username("john.doe")
                .email(email);
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builder_withNullUsername_throwsException() {
        Email email = Email.of("john@company.com");
        AuthenticatedUser.Builder builder = AuthenticatedUser.builder()
                .userId("user-123")
                .email(email);
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builder_withNullEmail_throwsException() {
        AuthenticatedUser.Builder builder = AuthenticatedUser.builder()
                .userId("user-123")
                .username("john.doe");
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }
}
