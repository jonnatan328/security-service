package com.company.security.authentication.infrastructure.adapter.output.token;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import com.company.security.authentication.domain.model.TokenPair;
import com.company.security.shared.domain.model.Email;
import com.company.security.shared.infrastructure.properties.JwtProperties;
import com.company.security.token.domain.exception.InvalidTokenException;
import com.company.security.token.domain.exception.TokenExpiredException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderAdapterTest {

    private static final String SECRET = "this-is-a-very-long-secret-key-for-testing-purposes-at-least-256-bits";
    private static final String ISSUER = "security-service";
    private static final long ACCESS_EXPIRATION = 900;
    private static final long REFRESH_EXPIRATION = 86400;
    private static final String USER_ID = "user-123";
    private static final String USERNAME = "john.doe";
    private static final String EMAIL = "john.doe@company.com";
    private static final String DEVICE_ID = "device-001";

    private JwtTokenProviderAdapter adapter;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setIssuer(ISSUER);
        jwtProperties.setAccessTokenExpiration(ACCESS_EXPIRATION);
        jwtProperties.setRefreshTokenExpiration(REFRESH_EXPIRATION);
        adapter = new JwtTokenProviderAdapter(jwtProperties);
    }

    @Test
    void generateTokenPair_withValidUser_returnsTokenPair() {
        AuthenticatedUser user = buildAuthenticatedUser();

        StepVerifier.create(adapter.generateTokenPair(user, DEVICE_ID))
                .assertNext(tokenPair -> {
                    assertThat(tokenPair.accessToken()).isNotBlank();
                    assertThat(tokenPair.refreshToken()).isNotBlank();
                    assertThat(tokenPair.tokenType()).isEqualTo("Bearer");
                    assertThat(tokenPair.accessTokenExpiresAt()).isAfter(Instant.now());
                    assertThat(tokenPair.refreshTokenExpiresAt()).isAfter(Instant.now());
                })
                .verifyComplete();
    }

    @Test
    void parseAccessToken_withValidToken_returnsTokenClaims() {
        AuthenticatedUser user = buildAuthenticatedUser();

        StepVerifier.create(adapter.generateTokenPair(user, DEVICE_ID)
                        .flatMap(tokenPair -> adapter.parseAccessToken(tokenPair.accessToken())))
                .assertNext(claims -> {
                    assertThat(claims.jti()).isNotBlank();
                    assertThat(claims.subject()).isEqualTo(USER_ID);
                    assertThat(claims.userId()).isEqualTo(USER_ID);
                    assertThat(claims.username()).isEqualTo(USERNAME);
                    assertThat(claims.email()).isEqualTo(EMAIL);
                    assertThat(claims.roles()).contains("ROLE_USER");
                    assertThat(claims.deviceId()).isEqualTo(DEVICE_ID);
                    assertThat(claims.issuer()).isEqualTo(ISSUER);
                })
                .verifyComplete();
    }

    @Test
    void parseRefreshToken_withValidToken_returnsTokenClaims() {
        AuthenticatedUser user = buildAuthenticatedUser();

        StepVerifier.create(adapter.generateTokenPair(user, DEVICE_ID)
                        .flatMap(tokenPair -> adapter.parseRefreshToken(tokenPair.refreshToken())))
                .assertNext(claims -> {
                    assertThat(claims.jti()).isNotBlank();
                    assertThat(claims.subject()).isEqualTo(USER_ID);
                    assertThat(claims.userId()).isEqualTo(USER_ID);
                })
                .verifyComplete();
    }

    @Test
    void parseAccessToken_withExpiredToken_throwsTokenExpiredException() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(USER_ID)
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now().minusSeconds(1000)))
                .expiration(Date.from(Instant.now().minusSeconds(100)))
                .claim("tokenType", "access")
                .claim("userId", USER_ID)
                .claim("username", USERNAME)
                .claim("email", EMAIL)
                .claim("roles", Set.of("ROLE_USER"))
                .claim("deviceId", DEVICE_ID)
                .signWith(key)
                .compact();

        StepVerifier.create(adapter.parseAccessToken(expiredToken))
                .expectError(TokenExpiredException.class)
                .verify();
    }

    @Test
    void parseAccessToken_withInvalidSignature_throwsInvalidTokenException() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-secret-key-that-is-long-enough-for-256-bits-hmac-sha".getBytes(StandardCharsets.UTF_8));
        String tokenWithWrongSig = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(USER_ID)
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(900)))
                .claim("tokenType", "access")
                .signWith(wrongKey)
                .compact();

        StepVerifier.create(adapter.parseAccessToken(tokenWithWrongSig))
                .expectError(InvalidTokenException.class)
                .verify();
    }

    @Test
    void parseAccessToken_withMalformedToken_throwsInvalidTokenException() {
        StepVerifier.create(adapter.parseAccessToken("not-a-valid-jwt"))
                .expectError(InvalidTokenException.class)
                .verify();
    }

    @Test
    void parseAccessToken_withWrongTokenType_throwsInvalidTokenException() {
        AuthenticatedUser user = buildAuthenticatedUser();

        StepVerifier.create(adapter.generateTokenPair(user, DEVICE_ID)
                        .flatMap(tokenPair -> adapter.parseAccessToken(tokenPair.refreshToken())))
                .expectError(InvalidTokenException.class)
                .verify();
    }

    @Test
    void parseRefreshToken_withAccessToken_throwsInvalidTokenException() {
        AuthenticatedUser user = buildAuthenticatedUser();

        StepVerifier.create(adapter.generateTokenPair(user, DEVICE_ID)
                        .flatMap(tokenPair -> adapter.parseRefreshToken(tokenPair.accessToken())))
                .expectError(InvalidTokenException.class)
                .verify();
    }

    @Test
    void extractJti_withValidToken_returnsJti() {
        AuthenticatedUser user = buildAuthenticatedUser();

        TokenPair tokenPair = adapter.generateTokenPair(user, DEVICE_ID).block();
        assertThat(tokenPair).isNotNull();

        String jti = adapter.extractJti(tokenPair.accessToken());
        assertThat(jti).isNotBlank();
    }

    @Test
    void extractJti_withInvalidToken_returnsNull() {
        assertThat(adapter.extractJti("not-a-valid-jwt")).isNull();
    }

    @Test
    void extractJti_withTokenWithoutJti_returnsNull() {
        assertThat(adapter.extractJti("a.eyJ0ZXN0IjoiMSJ9.c")).isNull();
    }

    @Test
    void parseAccessToken_withNullRoles_returnsEmptyRoles() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(USER_ID)
                .issuer(ISSUER)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(900)))
                .claim("tokenType", "access")
                .claim("userId", USER_ID)
                .claim("username", USERNAME)
                .claim("email", EMAIL)
                .claim("deviceId", DEVICE_ID)
                .signWith(key)
                .compact();

        StepVerifier.create(adapter.parseAccessToken(token))
                .assertNext(claims -> assertThat(claims.roles()).isEmpty())
                .verifyComplete();
    }

    private AuthenticatedUser buildAuthenticatedUser() {
        return AuthenticatedUser.builder()
                .userId(USER_ID)
                .username(USERNAME)
                .email(Email.of(EMAIL))
                .firstName("John")
                .lastName("Doe")
                .roles(Set.of("ROLE_USER"))
                .groups(Set.of("developers"))
                .enabled(true)
                .build();
    }
}
