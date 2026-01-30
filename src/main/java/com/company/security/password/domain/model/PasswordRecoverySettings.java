package com.company.security.password.domain.model;

public record PasswordRecoverySettings(long resetTokenExpirationSeconds, String resetBaseUrl) {}
