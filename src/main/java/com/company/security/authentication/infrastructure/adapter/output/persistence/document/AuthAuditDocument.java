package com.company.security.authentication.infrastructure.adapter.output.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for authentication audit logs.
 */
@Document(collection = "auth_audit_logs")
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

    public AuthAuditDocument() {
    }

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

    public static class RequestMetadata {
        private String ipAddress;
        private String userAgent;
        private String correlationId;

        public RequestMetadata() {
        }

        public RequestMetadata(String ipAddress, String userAgent, String correlationId) {
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
            this.correlationId = correlationId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public String getCorrelationId() {
            return correlationId;
        }

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public RequestMetadata getRequestMetadata() {
        return requestMetadata;
    }

    public void setRequestMetadata(RequestMetadata requestMetadata) {
        this.requestMetadata = requestMetadata;
    }
}
