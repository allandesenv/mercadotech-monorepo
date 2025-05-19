package com.mercadotech.authservice.controller;

import com.mercadotech.authservice.dto.UserDTO;
import com.mercadotech.authservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody UserDTO user) {
        if ("admin".equals(user.getUsername()) && "123456".equals(user.getPassword())) {
            return jwtUtil.generateToken(user.getUsername());
        }
        return "Credenciais inválidas";
    }

    @GetMapping("/validate")
    public String validate(@RequestHeader("Authorization") String token) {
        if (jwtUtil.validateToken(token.replace("Bearer ", ""))) {
            return "Token válido!";
        }
        return "Token inválido!";
    }
}
