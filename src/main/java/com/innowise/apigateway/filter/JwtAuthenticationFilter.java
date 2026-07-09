package com.innowise.apigateway.filter;

import com.innowise.apigateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        System.out.println("REQUEST PATH = " + path);

        if (isPublicEndpoint(path)) {
            System.out.println("PUBLIC ENDPOINT, SKIP JWT CHECK");
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        System.out.println("AUTHORIZATION HEADER = " + authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            System.out.println("AUTH HEADER IS MISSING OR INVALID");
            return unauthorized(exchange);
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        boolean isValid = jwtService.isAccessTokenValid(token);

        System.out.println("ACCESS TOKEN VALID = " + isValid);

        if (!isValid) {
            System.out.println("TOKEN IS INVALID");
            return unauthorized(exchange);
        }

        System.out.println("TOKEN IS VALID, REQUEST PASSED");

        return chain.filter(exchange);
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/auth/validate");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}