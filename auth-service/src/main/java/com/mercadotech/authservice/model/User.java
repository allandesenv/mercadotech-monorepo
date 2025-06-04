package com.mercadotech.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data // Lombok para getters, setters, toString, etc.
@NoArgsConstructor // Construtor sem argumentos
@AllArgsConstructor // Construtor com todos os argumentos
public class User {
    private String id; // ID do usuário, para ser o 'sub' do JWT
    private String username;
    private String password;
    private List<String> roles; // Lista de roles (ex: "GERENTE", "ESTOQUISTA")
}