package com.company.security.password.infrastructure.adapter.output.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "password_audit_logs")
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

    public PasswordAuditDocument() {}

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

    public static class RequestMetadata {
        private String ipAddress;
        private String userAgent;
        private String correlationId;

        public RequestMetadata() {}
        public RequestMetadata(String ipAddress, String userAgent, String correlationId) {
            this.ipAddress = ipAddress;
            this.userAgent = userAgent;
            this.correlationId = correlationId;
        }

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public RequestMetadata getRequestMetadata() { return requestMetadata; }
    public void setRequestMetadata(RequestMetadata requestMetadata) { this.requestMetadata = requestMetadata; }
}
