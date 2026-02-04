package com.company.security.password.infrastructure.adapter.output.persistence.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "password_reset_tokens")
@CompoundIndex(name = "userId_status_idx", def = "{'userId': 1, 'status': 1}")
@Getter
@Setter
public class PasswordResetTokenDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    @Indexed
    private String userId;

    private String email;

    private Instant createdAt;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    private Instant usedAt;

    private String status;

    private RequestMetadata requestMetadata;

    public PasswordResetTokenDocument() {}

    @Getter
    @Setter
    public static class RequestMetadata {
        private String ipAddress;
        private String userAgent;
        private String correlationId;

        public RequestMetadata(String ipAddress, String userAgent, String correlationId) {
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
            this.correlationId = correlationId;
        }

    }
}
