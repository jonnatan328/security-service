package com.company.security.authentication.domain.model;

import com.company.security.shared.domain.model.Email;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Value Object representing an authenticated user from the directory service.
 * Immutable and with no external dependencies.
 */
public final class AuthenticatedUser {

    private final String userId;
    private final String username;
    private final Email email;
    private final String firstName;
    private final String lastName;
    private final Set<String> roles;
    private final Set<String> groups;
    private final boolean enabled;

    private AuthenticatedUser(Builder builder) {
        this.userId = builder.userId;
        this.username = builder.username;
        this.email = builder.email;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.roles = builder.roles != null
                ? Collections.unmodifiableSet(builder.roles)
                : Collections.emptySet();
        this.groups = builder.groups != null
                ? Collections.unmodifiableSet(builder.groups)
                : Collections.emptySet();
        this.enabled = builder.enabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String userId() {
        return userId;
    }

    public String username() {
        return username;
    }

    public Email email() {
        return email;
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String fullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        return ((firstName != null ? firstName : "") + " " +
                (lastName != null ? lastName : "")).trim();
    }

    public Set<String> roles() {
        return roles;
    }

    public Set<String> groups() {
        return groups;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(Set<String> checkRoles) {
        return checkRoles.stream().anyMatch(roles::contains);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticatedUser that = (AuthenticatedUser) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "AuthenticatedUser{" +
               "userId='" + userId + '\'' +
               ", username='" + username + '\'' +
               ", email=" + email +
               ", enabled=" + enabled +
               '}';
    }

    public static final class Builder {
        private String userId;
        private String username;
        private Email email;
        private String firstName;
        private String lastName;
        private Set<String> roles;
        private Set<String> groups;
        private boolean enabled = true;

        private Builder() {}

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(Email email) {
            this.email = email;
            return this;
        }

        public Builder email(String email) {
            this.email = Email.of(email);
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder groups(Set<String> groups) {
            this.groups = groups;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public AuthenticatedUser build() {
            Objects.requireNonNull(userId, "userId cannot be null");
            Objects.requireNonNull(username, "username cannot be null");
            Objects.requireNonNull(email, "email cannot be null");
            return new AuthenticatedUser(this);
        }
    }
}
