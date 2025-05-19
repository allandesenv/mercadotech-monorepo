package com.mercadotech.authservice.dto;

public class LoginResponse {
    public String token;

    public LoginResponse(String token) {
        this.token = token;
    }
}