package com.company.security.password.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordChangeResultTest {

    @Test
    void success_createsSuccessfulResult() {
        PasswordChangeResult result = PasswordChangeResult.success("user-123", PasswordChangeResult.ChangeType.RESET);

        assertThat(result.userId()).isEqualTo("user-123");
        assertThat(result.changeType()).isEqualTo(PasswordChangeResult.ChangeType.RESET);
        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("Password changed successfully");
        assertThat(result.changedAt()).isNotNull();
    }

    @Test
    void failure_createsFailedResult() {
        PasswordChangeResult result = PasswordChangeResult.failure("user-123", PasswordChangeResult.ChangeType.UPDATE, "Invalid password");

        assertThat(result.userId()).isEqualTo("user-123");
        assertThat(result.changeType()).isEqualTo(PasswordChangeResult.ChangeType.UPDATE);
        assertThat(result.success()).isFalse();
        assertThat(result.message()).isEqualTo("Invalid password");
        assertThat(result.changedAt()).isNotNull();
    }

    @Test
    void builder_withAllFields_createsResult() {
        Instant changedAt = Instant.now();
        PasswordChangeResult result = PasswordChangeResult.builder()
                .userId("user-123")
                .changeType(PasswordChangeResult.ChangeType.RESET)
                .changedAt(changedAt)
                .success(true)
                .message("Done")
                .build();

        assertThat(result.userId()).isEqualTo("user-123");
        assertThat(result.changedAt()).isEqualTo(changedAt);
    }

    @Test
    void builder_withNullChangedAt_setsNow() {
        PasswordChangeResult result = PasswordChangeResult.builder()
                .userId("user-123")
                .changeType(PasswordChangeResult.ChangeType.RESET)
                .success(true)
                .build();

        assertThat(result.changedAt()).isNotNull();
    }

    @Test
    void builder_withNullUserId_throwsException() {
        PasswordChangeResult.Builder builder = PasswordChangeResult.builder()
                .changeType(PasswordChangeResult.ChangeType.RESET);
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void builder_withNullChangeType_throwsException() {
        PasswordChangeResult.Builder builder = PasswordChangeResult.builder()
                .userId("user-123");
        assertThatThrownBy(builder::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equals_withSameValues_returnsTrue() {
        Instant changedAt = Instant.now();
        PasswordChangeResult result1 = PasswordChangeResult.builder()
                .userId("user-123")
                .changeType(PasswordChangeResult.ChangeType.RESET)
                .changedAt(changedAt)
                .success(true)
                .build();

        PasswordChangeResult result2 = PasswordChangeResult.builder()
                .userId("user-123")
                .changeType(PasswordChangeResult.ChangeType.RESET)
                .changedAt(changedAt)
                .success(true)
                .build();

        assertThat(result1).isEqualTo(result2).hasSameHashCodeAs(result2);
    }

    @Test
    void equals_withSameInstance_returnsTrue() {
        PasswordChangeResult result = PasswordChangeResult.success("user-123", PasswordChangeResult.ChangeType.RESET);
        assertThat(result).isEqualTo(result);
    }

    @Test
    void equals_withNull_returnsFalse() {
        PasswordChangeResult result = PasswordChangeResult.success("user-123", PasswordChangeResult.ChangeType.RESET);
        assertThat(result).isNotEqualTo(null);
    }

    @Test
    void toString_containsRelevantInfo() {
        PasswordChangeResult result = PasswordChangeResult.success("user-123", PasswordChangeResult.ChangeType.RESET);
        assertThat(result.toString()).contains("user-123", "RESET");
    }
}
