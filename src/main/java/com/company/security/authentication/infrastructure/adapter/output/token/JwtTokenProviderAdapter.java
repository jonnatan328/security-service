package com.company.security.authentication.infrastructure.adapter.output.token;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.TokenClaims;
import com.company.security.authentication.domain.model.TokenPair;
import com.company.security.authentication.infrastructure.application.port.output.TokenProviderPort;
import com.company.security.shared.infrastructure.properties.JwtProperties;
import com.company.security.token.domain.exception.InvalidTokenException;
import com.company.security.token.domain.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT Token Provider adapter implementation.
 * Handles JWT token generation and parsing.
 */
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProviderAdapter.class);

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_DEVICE_ID = "deviceId";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;

    public JwtTokenProviderAdapter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.accessTokenKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshTokenKey = Keys.hmacShaKeyFor(
                (jwtProperties.getSecret() + "-refresh").getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<TokenPair> generateTokenPair(AuthenticatedUser user, String deviceId) {
        return Mono.fromCallable(() -> {
            Instant now = Instant.now();

            // Generate access token
            Instant accessExpiry = now.plusSeconds(jwtProperties.getAccessTokenExpiration());
            String accessToken = generateToken(user, deviceId, now, accessExpiry, TOKEN_TYPE_ACCESS, accessTokenKey);

            // Generate refresh token
            Instant refreshExpiry = now.plusSeconds(jwtProperties.getRefreshTokenExpiration());
            String refreshToken = generateToken(user, deviceId, now, refreshExpiry, TOKEN_TYPE_REFRESH, refreshTokenKey);

            return TokenPair.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiresAt(accessExpiry)
                    .refreshTokenExpiresAt(refreshExpiry)
                    .tokenType("Bearer")
                    .build();
        });
    }

    @Override
    public Mono<TokenClaims> parseAccessToken(String accessToken) {
        return parseToken(accessToken, accessTokenKey, TOKEN_TYPE_ACCESS);
    }

    @Override
    public Mono<TokenClaims> parseRefreshToken(String refreshToken) {
        return parseToken(refreshToken, refreshTokenKey, TOKEN_TYPE_REFRESH);
    }

    @Override
    public String extractJti(String token) {
        try {
            // Parse without validation to extract JTI
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            // Simple extraction - in production, use proper JSON parsing
            int jtiStart = payload.indexOf("\"jti\":\"");
            if (jtiStart == -1) {
                return null;
            }
            jtiStart += 7;
            int jtiEnd = payload.indexOf("\"", jtiStart);
            return payload.substring(jtiStart, jtiEnd);
        } catch (Exception e) {
            log.debug("Failed to extract JTI from token: {}", e.getMessage());
            return null;
        }
    }

    private String generateToken(
            AuthenticatedUser user,
            String deviceId,
            Instant issuedAt,
            Instant expiresAt,
            String tokenType,
            SecretKey key) {

        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(user.username())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim(CLAIM_USER_ID, user.userId())
                .claim(CLAIM_USERNAME, user.username())
                .claim(CLAIM_EMAIL, user.email().value())
                .claim(CLAIM_ROLES, user.roles())
                .claim(CLAIM_DEVICE_ID, deviceId)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .signWith(key)
                .compact();
    }

    private Mono<TokenClaims> parseToken(String token, SecretKey key, String expectedTokenType) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .requireIssuer(jwtProperties.getIssuer())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Verify token type
                String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
                if (!expectedTokenType.equals(tokenType)) {
                    throw new InvalidTokenException("Invalid token type");
                }

                // Extract roles
                @SuppressWarnings("unchecked")
                List<String> rolesList = claims.get(CLAIM_ROLES, List.class);
                Set<String> roles = rolesList != null
                        ? new HashSet<>(rolesList)
                        : Collections.emptySet();

                return TokenClaims.builder()
                        .jti(claims.getId())
                        .subject(claims.getSubject())
                        .userId(claims.get(CLAIM_USER_ID, String.class))
                        .username(claims.get(CLAIM_USERNAME, String.class))
                        .email(claims.get(CLAIM_EMAIL, String.class))
                        .roles(roles)
                        .deviceId(claims.get(CLAIM_DEVICE_ID, String.class))
                        .issuedAt(claims.getIssuedAt().toInstant())
                        .expiresAt(claims.getExpiration().toInstant())
                        .issuer(claims.getIssuer())
                        .build();

            } catch (ExpiredJwtException e) {
                log.debug("Token expired: {}", e.getMessage());
                throw new TokenExpiredException();
            } catch (SignatureException e) {
                log.warn("Invalid token signature: {}", e.getMessage());
                throw new InvalidTokenException("Invalid token signature");
            } catch (MalformedJwtException e) {
                log.warn("Malformed token: {}", e.getMessage());
                throw new InvalidTokenException("Malformed token");
            } catch (InvalidTokenException e) {
                throw e;
            } catch (Exception e) {
                log.error("Token parsing error: {}", e.getMessage());
                throw new InvalidTokenException("Failed to parse token", e);
            }
        });
    }
}
