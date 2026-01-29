package com.company.security.password.infrastructure.adapter.output.persistence.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "password_reset_tokens")
@CompoundIndex(name = "userId_status_idx", def = "{'userId': 1, 'status': 1}")
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
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public RequestMetadata getRequestMetadata() { return requestMetadata; }
    public void setRequestMetadata(RequestMetadata requestMetadata) { this.requestMetadata = requestMetadata; }
}
