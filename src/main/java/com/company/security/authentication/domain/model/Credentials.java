package com.company.security.authentication.domain.model;

import java.util.Objects;

/**
 * Value Object representing user credentials for authentication.
 * Immutable and with no external dependencies.
 */
public final class Credentials {

    private final String username;
    private final String password;
    private final String deviceId;

    private Credentials(String username, String password, String deviceId) {
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
    }

    public static Credentials of(String username, String password, String deviceId) {
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");

        if (username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }

        String normalizedDeviceId = deviceId != null && !deviceId.isBlank()
                ? deviceId.trim()
                : "default";

        return new Credentials(username.trim(), password, normalizedDeviceId);
    }

    public static Credentials of(String username, String password) {
        return of(username, password, null);
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String deviceId() {
        return deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credentials that = (Credentials) o;
        return Objects.equals(username, that.username) &&
               Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, deviceId);
    }

    @Override
    public String toString() {
        return "Credentials{username='" + username + "', deviceId='" + deviceId + "'}";
    }
}
