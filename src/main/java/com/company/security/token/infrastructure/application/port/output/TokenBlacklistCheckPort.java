package com.company.security.token.infrastructure.application.port.output;

import reactor.core.publisher.Mono;

public interface TokenBlacklistCheckPort {

    Mono<Boolean> isBlacklisted(String jti);
}
