package com.mercadotech.authservice.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List; // Adicione este import
import java.util.Map; // Adicione este import
import java.util.HashMap; // Adicione este import

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration; // Em milissegundos

    /**
     * Gera um token JWT contendo o ID do usuário e suas roles.
     * @param userId O ID único do usuário.
     * @param username O nome de usuário.
     * @param roles Uma lista de strings representando as roles do usuário (ex: "GERENTE", "ESTOQUISTA").
     * @return O token JWT gerado.
     */
    public String generateToken(String userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // Adicionar claims personalizadas
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles); // Adiciona a lista de roles como uma claim "roles"

        return Jwts.builder()
                .setClaims(claims) // Adiciona as claims personalizadas
                .setSubject(userId) // O 'sub' (subject) do JWT será o ID do usuário
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)  // << AQUI
                .compact();
    }
}