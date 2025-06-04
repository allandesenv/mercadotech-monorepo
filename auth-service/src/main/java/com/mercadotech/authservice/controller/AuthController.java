package com.mercadotech.authservice.controller;

import com.mercadotech.authservice.dto.LoginRequest;
import com.mercadotech.authservice.dto.LoginResponse;
import com.mercadotech.authservice.jwt.JwtUtil;
import com.mercadotech.authservice.model.User; // Adicione este import
import com.mercadotech.authservice.model.UserRepository; // Adicione este import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository; // Injete o novo UserRepository

    public AuthController(JwtUtil jwtUtil, UserRepository userRepository) { // Ajuste o construtor
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Busca o usuário no repositório em memória
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null); // Retorna null se não encontrar

        // Valida as credenciais
        if (user != null && user.getPassword().equals(request.getPassword())) {
            // Gera o token com o ID do usuário e suas roles
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoles());
            return ResponseEntity.ok(new LoginResponse(token));
        } else {
            return ResponseEntity.status(401).build(); // Retorna 401 Unauthorized para credenciais inválidas
        }
    }

    // Endpoint de teste público (não requer autenticação no Gateway)
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Auth Service!";
    }
}