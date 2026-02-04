package com.company.security.password.infrastructure.adapter.output.persistence.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "password_audit_logs")
@Getter
@Setter
public class PasswordAuditDocument {

    @Id
    private String id;

    @Indexed
    private String eventType;

    @Indexed
    private String userId;

    private String email;

    @Indexed
    private Instant timestamp;

    private boolean success;

    private String failureReason;

    private RequestMetadata requestMetadata;

    public PasswordAuditDocument(String eventType, String userId, String email,
                                  boolean success, String failureReason, RequestMetadata requestMetadata) {
        this.eventType = eventType;
        this.userId = userId;
        this.email = email;
        this.timestamp = Instant.now();
        this.success = success;
        this.failureReason = failureReason;
        this.requestMetadata = requestMetadata;
    }

    @Setter
    @Getter
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
