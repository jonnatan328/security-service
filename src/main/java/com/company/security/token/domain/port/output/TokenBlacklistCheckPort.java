package com.company.security.token.domain.port.output;

import reactor.core.publisher.Mono;

public interface TokenBlacklistCheckPort {

    Mono<Boolean> isBlacklisted(String jti);
}
