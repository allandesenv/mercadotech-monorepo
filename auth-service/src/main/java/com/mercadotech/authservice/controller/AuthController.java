package com.mercadotech.authservice.controller;

import com.mercadotech.authservice.dto.LoginRequest;
import com.mercadotech.authservice.dto.LoginResponse;
import com.mercadotech.authservice.jwt.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if ("admin".equals(request.getUsername()) && "admin".equals(request.getPassword())) {
            String token = jwtUtil.generateToken(request.getUsername());
            return ResponseEntity.ok(new LoginResponse(token));
        } else {
            return ResponseEntity.status(401).build();
        }
    }
}
