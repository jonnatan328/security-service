package com.company.security.token.domain.port.output;

import com.company.security.token.domain.model.Token;
import reactor.core.publisher.Mono;

public interface TokenIntrospectionPort {

    Mono<Token> introspect(String rawToken);
}
