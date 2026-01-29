# Microservicio de Seguridad - Spring Boot + Screaming Architecture + NoSQL

## Contexto
Construye un microservicio de **Seguridad/Autenticación** usando Screaming Architecture. El servicio maneja exclusivamente autenticación contra Active Directory/LDAP y gestión de contraseñas. Usa MongoDB como base de datos NoSQL para persistencia y Redis para cache/blacklist.

## Principios de Arquitectura

### Screaming Architecture
- La estructura del proyecto "grita" su propósito de negocio
- Organizado por **features** (authentication, password, token), no por capas técnicas
- Cada feature es autónomo y contiene todo lo necesario para funcionar

### Ubicación de Capas
- **domain/**: Núcleo puro, CERO dependencias externas (ni Spring, ni librerías)
- **infrastructure/**: Todo lo que tiene dependencias externas, incluyendo:
  - **application/**: Puertos y casos de uso (puede usar anotaciones de Spring)
  - **adapter/**: Implementaciones de entrada (REST) y salida (DB, Redis, LDAP, clientes HTTP)
  - **config/**: Configuración de beans del feature

## Dominio de Negocio: Security Service

### Features y Funcionalidades

#### 1. Authentication Feature
- **Sign In**: Autenticación contra Active Directory/LDAP
- **Sign Out**: Invalidación de tokens (blacklist en Redis)
- **Refresh Token**: Renovación de access token usando refresh token

#### 2. Password Management Feature
- **Recover Password**: Genera token de recuperación y publica evento para notificación
- **Reset Password**: Cambio de contraseña usando token de recuperación
- **Update Password**: Cambio de contraseña autenticado (requiere password actual)

#### 3. Token Feature (interno, service-to-service)
- **Validate Token**: Validación de JWT para otros microservicios

### Fuera de Alcance (otros microservicios)
- ❌ Sign Up → Client Service
- ❌ User Profile → Client Service
- ❌ Envío de notificaciones → Notification Service (este servicio solo publica eventos)

## Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 LTS | Lenguaje |
| Spring Boot | 3.2+ | Framework base |
| Spring WebFlux | - | Programación reactiva |
| Spring Security | - | Seguridad reactiva + JWT |
| Spring Data MongoDB Reactive | - | Persistencia NoSQL |
| Spring Data Redis Reactive | - | Cache, blacklist, rate limiting |
| Spring LDAP | - | Conexión Active Directory/LDAP |
| Gradle Kotlin DSL | 8.x | Build tool |
| jjwt | 0.12.x | Generación/validación JWT |
| Resilience4j | 2.2.x | Retry, Circuit Breaker, Rate Limiter |
| MapStruct | 1.5.x | Mapeo de objetos |
| SpringDoc OpenAPI | 2.3.x | Documentación API |

## Estructura de Paquetes
```
src/main/java/com/company/security/
│
├── authentication/                                # 🔐 FEATURE: Authentication
│   ├── domain/
│   │   ├── model/
│   │   │   ├── AuthenticatedUser.java
│   │   │   ├── Credentials.java
│   │   │   ├── TokenPair.java
│   │   │   ├── TokenClaims.java
│   │   │   └── AuthenticationResult.java
│   │   ├── exception/
│   │   │   ├── AuthenticationException.java
│   │   │   ├── InvalidCredentialsException.java
│   │   │   ├── AccountLockedException.java
│   │   │   ├── AccountDisabledException.java
│   │   │   └── DirectoryServiceException.java
│   │   └── service/
│   │       └── AuthenticationDomainService.java
│   │
│   └── infrastructure/
│       ├── application/
│       │   ├── port/
│       │   │   ├── input/
│       │   │   │   ├── SignInUseCase.java
│       │   │   │   ├── SignOutUseCase.java
│       │   │   │   └── RefreshTokenUseCase.java
│       │   │   └── output/
│       │   │       ├── DirectoryServicePort.java
│       │   │       ├── TokenProviderPort.java
│       │   │       ├── TokenBlacklistPort.java
│       │   │       ├── RefreshTokenPort.java
│       │   │       └── AuthAuditPort.java
│       │   └── usecase/
│       │       ├── SignInUseCaseImpl.java
│       │       ├── SignOutUseCaseImpl.java
│       │       └── RefreshTokenUseCaseImpl.java
│       ├── adapter/
│       │   ├── input/rest/
│       │   │   ├── handler/
│       │   │   │   └── AuthenticationHandler.java
│       │   │   ├── router/
│       │   │   │   └── AuthenticationRouter.java
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   │   ├── SignInRequest.java
│       │   │   │   │   ├── SignOutRequest.java
│       │   │   │   │   └── RefreshTokenRequest.java
│       │   │   │   └── response/
│       │   │   │       ├── AuthenticationResponse.java
│       │   │   │       └── TokenResponse.java
│       │   │   └── mapper/
│       │   │       └── AuthenticationRestMapper.java
│       │   └── output/
│       │       ├── directory/
│       │       │   ├── LdapDirectoryAdapter.java
│       │       │   ├── ActiveDirectoryAdapter.java
│       │       │   └── DirectoryUserMapper.java
│       │       ├── token/
│       │       │   ├── JwtTokenProviderAdapter.java
│       │       │   ├── TokenBlacklistRedisAdapter.java
│       │       │   └── RefreshTokenRedisAdapter.java
│       │       └── persistence/
│       │           ├── AuthAuditMongoAdapter.java
│       │           ├── repository/
│       │           │   └── AuthAuditRepository.java
│       │           └── document/
│       │               └── AuthAuditDocument.java
│       └── config/
│           └── AuthenticationBeanConfig.java
│
├── password/                                      # 🔑 FEATURE: Password Management
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Password.java
│   │   │   ├── PasswordResetToken.java
│   │   │   ├── PasswordPolicy.java
│   │   │   └── PasswordChangeResult.java
│   │   ├── exception/
│   │   │   ├── PasswordException.java
│   │   │   ├── PasswordValidationException.java
│   │   │   ├── PasswordResetTokenExpiredException.java
│   │   │   ├── PasswordResetTokenInvalidException.java
│   │   │   ├── CurrentPasswordMismatchException.java
│   │   │   └── PasswordHistoryViolationException.java
│   │   └── service/
│   │       └── PasswordPolicyService.java
│   │
│   └── infrastructure/
│       ├── application/
│       │   ├── port/
│       │   │   ├── input/
│       │   │   │   ├── RecoverPasswordUseCase.java
│       │   │   │   ├── ResetPasswordUseCase.java
│       │   │   │   └── UpdatePasswordUseCase.java
│       │   │   └── output/
│       │   │       ├── PasswordResetTokenPort.java
│       │   │       ├── DirectoryPasswordPort.java
│       │   │       ├── EventPublisherPort.java
│       │   │       ├── UserLookupPort.java
│       │   │       └── PasswordAuditPort.java
│       │   └── usecase/
│       │       ├── RecoverPasswordUseCaseImpl.java
│       │       ├── ResetPasswordUseCaseImpl.java
│       │       └── UpdatePasswordUseCaseImpl.java
│       ├── adapter/
│       │   ├── input/rest/
│       │   │   ├── handler/
│       │   │   │   └── PasswordHandler.java
│       │   │   ├── router/
│       │   │   │   └── PasswordRouter.java
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   │   ├── RecoverPasswordRequest.java
│       │   │   │   │   ├── ResetPasswordRequest.java
│       │   │   │   │   └── UpdatePasswordRequest.java
│       │   │   │   └── response/
│       │   │   │       └── PasswordOperationResponse.java
│       │   │   └── mapper/
│       │   │       └── PasswordRestMapper.java
│       │   └── output/
│       │       ├── persistence/
│       │       │   ├── PasswordResetTokenMongoAdapter.java
│       │       │   ├── PasswordAuditMongoAdapter.java
│       │       │   ├── repository/
│       │       │   │   ├── PasswordResetTokenRepository.java
│       │       │   │   └── PasswordAuditRepository.java
│       │       │   └── document/
│       │       │       ├── PasswordResetTokenDocument.java
│       │       │       └── PasswordAuditDocument.java
│       │       ├── directory/
│       │       │   └── DirectoryPasswordAdapter.java
│       │       ├── messaging/
│       │       │   └── PasswordEventPublisherAdapter.java
│       │       └── client/
│       │           ├── ClientServiceAdapter.java
│       │           └── dto/
│       │               └── UserInfoClientResponse.java
│       └── config/
│           └── PasswordBeanConfig.java
│
├── token/                                         # 🎫 FEATURE: Token Validation (Internal)
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Token.java
│   │   │   ├── TokenValidationResult.java
│   │   │   └── TokenStatus.java
│   │   └── exception/
│   │       ├── TokenException.java
│   │       ├── InvalidTokenException.java
│   │       ├── TokenExpiredException.java
│   │       └── TokenRevokedException.java
│   │
│   └── infrastructure/
│       ├── application/
│       │   ├── port/
│       │   │   ├── input/
│       │   │   │   └── ValidateTokenUseCase.java
│       │   │   └── output/
│       │   │       ├── TokenIntrospectionPort.java
│       │   │       └── TokenBlacklistCheckPort.java
│       │   └── usecase/
│       │       └── ValidateTokenUseCaseImpl.java
│       ├── adapter/
│       │   └── input/rest/
│       │       ├── handler/
│       │       │   └── TokenValidationHandler.java
│       │       ├── router/
│       │       │   └── InternalTokenRouter.java
│       │       ├── dto/
│       │       │   ├── request/
│       │       │   │   └── ValidateTokenRequest.java
│       │       │   └── response/
│       │       │       └── TokenValidationResponse.java
│       │       └── mapper/
│       │           └── TokenRestMapper.java
│       └── config/
│           └── TokenBeanConfig.java
│
├── shared/                                        # 🔧 SHARED: Cross-cutting concerns
│   ├── domain/
│   │   ├── model/
│   │   │   └── Email.java
│   │   └── exception/
│   │       ├── DomainException.java
│   │       └── ErrorCode.java
│   │
│   └── infrastructure/
│       ├── config/
│       │   ├── security/
│       │   │   ├── SecurityConfig.java
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   ├── JwtAuthenticationManager.java
│       │   │   ├── JwtAuthenticationConverter.java
│       │   │   ├── SecurityContextRepository.java
│       │   │   ├── CustomAuthenticationEntryPoint.java
│       │   │   └── CustomAccessDeniedHandler.java
│       │   ├── database/
│       │   │   ├── MongoConfig.java
│       │   │   └── RedisConfig.java
│       │   ├── ldap/
│       │   │   └── LdapConfig.java
│       │   ├── resilience/
│       │   │   └── ResilienceConfig.java
│       │   ├── web/
│       │   │   ├── WebClientConfig.java
│       │   │   └── CorsConfig.java
│       │   ├── messaging/
│       │   │   └── KafkaConfig.java
│       │   └── openapi/
│       │       └── OpenApiConfig.java
│       ├── adapter/output/security/
│       │   ├── JwtTokenProvider.java
│       │   └── BcryptPasswordEncoderAdapter.java
│       ├── filter/
│       │   ├── CorrelationIdFilter.java
│       │   └── RequestLoggingFilter.java
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java
│       │   └── dto/
│       │       └── ErrorResponse.java
│       └── properties/
│           ├── JwtProperties.java
│           ├── LdapProperties.java
│           ├── ActiveDirectoryProperties.java
│           ├── SecurityProperties.java
│           ├── PasswordPolicyProperties.java
│           ├── ResilienceProperties.java
│           └── ServicesProperties.java
│
└── SecurityServiceApplication.java
```

## Colecciones MongoDB

### password_reset_tokens
```json
{
  "_id": "ObjectId",
  "token": "uuid-string (unique index)",
  "userId": "string",
  "email": "string",
  "createdAt": "ISODate",
  "expiresAt": "ISODate (TTL index)",
  "usedAt": "ISODate | null",
  "status": "PENDING | USED | EXPIRED | CANCELLED",
  "requestMetadata": {
    "ipAddress": "string",
    "userAgent": "string",
    "correlationId": "string"
  }
}
```

### auth_audit_logs
```json
{
  "_id": "ObjectId",
  "eventType": "SIGN_IN_SUCCESS | SIGN_IN_FAILED | SIGN_OUT | TOKEN_REFRESH | TOKEN_REVOKED",
  "userId": "string | null",
  "username": "string",
  "timestamp": "ISODate",
  "success": "boolean",
  "failureReason": "string | null",
  "requestMetadata": {
    "ipAddress": "string",
    "userAgent": "string",
    "correlationId": "string"
  }
}
```

### password_audit_logs
```json
{
  "_id": "ObjectId",
  "eventType": "PASSWORD_RESET_REQUESTED | PASSWORD_RESET_COMPLETED | PASSWORD_UPDATED | PASSWORD_RESET_FAILED",
  "userId": "string",
  "email": "string",
  "timestamp": "ISODate",
  "success": "boolean",
  "failureReason": "string | null",
  "requestMetadata": {
    "ipAddress": "string",
    "userAgent": "string",
    "correlationId": "string"
  }
}
```

## Redis Keys Structure
```
security:blacklist:{jti}                    -> "1" (TTL: token remaining lifetime)
security:refresh:{userId}:{deviceId}        -> JSON (TTL: refresh token lifetime)
security:ratelimit:signin:{ip}              -> count (TTL: 1 minute)
security:ratelimit:recovery:{email}         -> count (TTL: 1 hour)
security:failed-attempts:{username}         -> count (TTL: lock duration)
```

## Eventos a Publicar (Kafka/Message Broker)

El servicio NO envía notificaciones directamente. Publica eventos para que el Notification Service los consuma:
```json
// Topic: security.password.events
{
  "eventType": "PASSWORD_RESET_REQUESTED",
  "eventId": "uuid",
  "timestamp": "ISO8601",
  "payload": {
    "userId": "string",
    "email": "string",
    "resetToken": "string",
    "expiresAt": "ISO8601",
    "resetUrl": "string"
  },
  "metadata": {
    "correlationId": "string",
    "source": "security-service"
  }
}
```

## Archivos de Configuración Requeridos

### 1. build.gradle.kts
Incluir plugins y dependencias para:
- Spring Boot 3.2+ con WebFlux
- Spring Security Reactive
- Spring Data MongoDB Reactive
- Spring Data Redis Reactive
- Spring LDAP + UnboundID LDAP SDK
- Spring Kafka (para eventos)
- JWT (jjwt 0.12.x)
- Resilience4j Reactor
- MapStruct + Lombok
- SpringDoc OpenAPI WebFlux
- Micrometer Prometheus
- Testing: JUnit 5, Reactor Test, Testcontainers (MongoDB, Redis), ArchUnit, UnboundID (in-memory LDAP)
- Quality: JaCoCo (80% mínimo, 90% en usecases), SonarQube, SpotBugs, Pitest (70% mutation)
- JFrog Artifactory

### 2. Archivos de Properties
```
src/main/resources/
├── application.yml              # Configuración base con placeholders
├── application-local.yml        # Desarrollo local
├── application-dev.yml          # Ambiente desarrollo
├── application-qa.yml           # Ambiente QA
├── application-prod.yml         # Ambiente producción
└── logback-spring.xml           # Logging por ambiente (JSON en prod)
```

#### Configuraciones clave en application.yml:
- spring.data.mongodb (uri, auto-index-creation)
- spring.data.redis (host, port, password, pool)
- spring.kafka (bootstrap-servers, producer config)
- ldap (url, base, manager-dn, manager-password, user-search-base, user-search-filter)
- ldap.active-directory (enabled, domain) - configurable para cuando se defina el proveedor
- security.jwt (secret, access-token-expiration, refresh-token-expiration, issuer)
- security.password (reset-token-expiration, min-length, max-length, require-uppercase, require-lowercase, require-digit, require-special-char, max-failed-attempts, lock-duration)
- security.cors (allowed-origins, allowed-methods, allowed-headers)
- resilience4j.circuitbreaker.instances (directoryService, clientService)
- resilience4j.retry.instances (directoryService, clientService)
- resilience4j.ratelimiter.instances (signIn, passwordRecovery, tokenValidation)
- resilience4j.timelimiter.instances (directoryService)
- services.client-service (base-url, timeout)
- management.endpoints (health, metrics, prometheus)
- springdoc (api-docs path, swagger-ui)

### 3. logback-spring.xml
- Profile local/dev: Console appender con formato legible y colores
- Profile qa/prod: Console appender con formato JSON estructurado
- MDC para correlationId en todos los logs
- Niveles configurables por paquete

### 4. Archivos de Calidad
- config/spotbugs/spotbugs-exclude.xml (excluir DTOs, documents, config, exceptions)
- Configuración JaCoCo en build.gradle.kts con exclusiones y umbrales
- Configuración SonarQube en build.gradle.kts
- Configuración Pitest enfocada en usecases

### 5. Dockerfile
Multi-stage optimizado:
- Stage build: eclipse-temurin:21-jdk-alpine + Gradle
- Stage runtime: eclipse-temurin:21-jre-alpine
- Extracción de layers para mejor caching
- Usuario no-root (appuser:appgroup)
- HEALTHCHECK contra /actuator/health
- JAVA_OPTS optimizados para contenedores (UseContainerSupport, MaxRAMPercentage, G1GC)

### 6. Kubernetes Manifests
```
k8s/
├── base/
│   ├── deployment.yaml          # Con probes, resources, security context
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── secret.yaml              # Template con placeholders
│   ├── hpa.yaml                 # Autoscaling CPU/Memory
│   ├── serviceaccount.yaml
│   └── kustomization.yaml
└── overlays/
    ├── dev/
    │   ├── kustomization.yaml
    │   └── patches/
    ├── qa/
    │   ├── kustomization.yaml
    │   └── patches/
    └── prod/
        ├── kustomization.yaml
        └── patches/
```

### 7. OpenAPI/Swagger
Documentar todos los endpoints con:
- Tags por feature (Authentication, Password Management, Token - Internal)
- Ejemplos de request/response
- Códigos de error documentados
- Esquemas de error siguiendo RFC 7807 Problem Details

## Spring Security Configuration

### Endpoints Públicos (sin autenticación)
- POST /api/v1/auth/signin
- POST /api/v1/auth/refresh
- POST /api/v1/password/recover
- POST /api/v1/password/reset
- GET /actuator/health/**
- GET /api-docs/**, /swagger-ui/**

### Endpoints Autenticados (requieren JWT válido)
- POST /api/v1/auth/signout
- POST /api/v1/password/update

### Endpoints Internos (service-to-service, requieren scope específico)
- POST /internal/v1/token/validate

### Componentes de Seguridad
- SecurityConfig: SecurityWebFilterChain con reglas
- JwtAuthenticationFilter: Extrae y valida JWT del header
- JwtAuthenticationManager: Valida token contra blacklist y extrae claims
- JwtAuthenticationConverter: Convierte ServerWebExchange a Authentication
- SecurityContextRepository: NoOp (stateless)
- CustomAuthenticationEntryPoint: Respuesta 401 estandarizada
- CustomAccessDeniedHandler: Respuesta 403 estandarizada

## LDAP/Active Directory Configuration

El proveedor de directorio aún no está definido, por lo tanto:
- Crear DirectoryServicePort como interfaz agnóstica
- Implementar LdapDirectoryAdapter genérico
- Implementar ActiveDirectoryAdapter específico para AD
- Configurar mediante properties cuál usar (ldap.active-directory.enabled)
- Mapeo de atributos configurable (uid, mail, sAMAccountName, givenName, sn, memberOf)
- Pool de conexiones con Spring LDAP
- Circuit breaker y retry en todas las operaciones de directorio
- Time limiter para operaciones de autenticación

## Testing Strategy

### Unit Tests
- domain/service/* → Lógica de dominio pura
- infrastructure/application/usecase/* → Casos de uso con mocks de puertos

### Integration Tests
- adapter/input/rest/* → WebTestClient + StepVerifier
- adapter/output/persistence/* → Testcontainers MongoDB
- adapter/output/token/* → Testcontainers Redis
- adapter/output/directory/* → UnboundID in-memory LDAP

### Architecture Tests (ArchUnit)
- domain no depende de infrastructure
- domain no depende de Spring ni librerías externas
- application.port.input no depende de adapters
- Cada feature solo depende de shared y su propio código
- Adapters implementan sus respectivos ports

## Instrucciones de Generación

1. Crear estructura completa de directorios según el árbol de paquetes
2. Implementar dominios de cada feature (models como Value Objects inmutables, excepciones específicas, servicios de dominio puros)
3. Implementar puertos de entrada y salida como interfaces
4. Implementar casos de uso con inyección de puertos
5. Implementar adaptadores REST (handlers funcionales + routers)
6. Implementar adaptadores de salida (MongoDB, Redis, LDAP, Kafka, HTTP client)
7. Crear configuración de beans por feature
8. Crear configuración compartida (security, database, ldap, resilience)
9. Crear archivos de properties por ambiente
10. Crear logback-spring.xml
11. Crear build.gradle.kts completo
12. Crear Dockerfile multi-stage
13. Crear manifests de Kubernetes con Kustomize
14. Crear configuración OpenAPI
15. Implementar tests unitarios, de integración y de arquitectura
16. Configurar herramientas de calidad (JaCoCo, Sonar, SpotBugs, Pitest)

**IMPORTANTE**:
- El proyecto debe compilar sin errores
- Todos los tests deben pasar
- La cobertura de código debe ser >= 80%
- La cobertura de mutación en usecases debe ser >= 70%
- No debe haber violaciones de SpotBugs en código principal
