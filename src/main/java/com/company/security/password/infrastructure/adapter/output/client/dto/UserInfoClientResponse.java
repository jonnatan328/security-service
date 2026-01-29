package com.company.security.password.infrastructure.adapter.output.client.dto;

public record UserInfoClientResponse(
        String userId,
        String email,
        String username,
        String firstName,
        String lastName
) {
}
