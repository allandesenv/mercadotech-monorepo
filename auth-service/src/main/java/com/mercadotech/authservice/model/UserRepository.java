package com.mercadotech.authservice.model;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct; // Importe este para @PostConstruct
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class UserRepository {

    private final Map<String, User> users = new HashMap<>();

    @PostConstruct // Este método será executado após a injeção de dependências e inicialização do bean
    public void init() {
        // Usuários de teste em memória com suas roles
        users.put("gerente", new User("user-101", "gerente", "senha123", Arrays.asList("GERENTE", "CAIXA", "ESTOQUISTA")));
        users.put("estoquista", new User("user-102", "estoquista", "senha123", Arrays.asList("ESTOQUISTA")));
        users.put("caixa", new User("user-103", "caixa", "senha123", Arrays.asList("CAIXA")));
        users.put("admin", new User("user-001", "admin", "admin", Arrays.asList("ADMIN", "GERENTE", "ESTOQUISTA", "CAIXA"))); // Seu usuário admin existente
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }
}