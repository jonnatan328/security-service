package com.company.security.authentication.infrastructure.adapter.output.persistence.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for authentication audit logs.
 */
@Document(collection = "auth_audit_logs")
@Getter
@Setter
public class AuthAuditDocument {

    @Id
    private String id;

    @Indexed
    private String eventType;

    @Indexed
    private String userId;

    @Indexed
    private String username;

    @Indexed
    private Instant timestamp;

    private boolean success;

    private String failureReason;

    private RequestMetadata requestMetadata;

    public AuthAuditDocument(
            String eventType,
            String userId,
            String username,
            boolean success,
            String failureReason,
            RequestMetadata requestMetadata) {
        this.eventType = eventType;
        this.userId = userId;
        this.username = username;
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
