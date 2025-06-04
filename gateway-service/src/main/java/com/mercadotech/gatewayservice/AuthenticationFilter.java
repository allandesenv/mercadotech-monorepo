package com.mercadotech.gatewayservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Order(1) // Executa antes dos demais filtros
public class AuthenticationFilter implements GlobalFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Endpoints públicos que não exigem autenticação
    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/auth/login",
            "/auth/hello",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Permite acesso direto a endpoints públicos
        if (isOpenEndpoint(request)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);

            if (userId == null || userId.isBlank()) {
                return unauthorized("User ID not found in token claims");
            }

            if (roles == null || roles.isEmpty()) {
                return unauthorized("Roles not found or empty in token claims");
            }

            // Propaga informações de autenticação nos headers para os microsserviços
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-Auth-User-Id", userId)
                    .header("X-Auth-User-Roles", String.join(",", roles))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return unauthorized("Invalid or expired token: " + e.getMessage());
        }
    }

    private boolean isOpenEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        // Verifica se o path começa com algum endpoint público
        return OPEN_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(String message) {
        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, message));
    }
}
