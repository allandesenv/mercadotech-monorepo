package com.mercadotech.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Order(1)
public class AuthenticationFilter implements GlobalFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/auth/login"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isOpenEndpoint(request)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return this.unauthorized("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Você pode acessar claims.get("sub") ou outros dados aqui

        } catch (Exception e) {
            return this.unauthorized("Invalid or expired token");
        }

        return chain.filter(exchange);
    }

    private boolean isOpenEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return OPEN_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(String message) {
        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, message));
    }
}
