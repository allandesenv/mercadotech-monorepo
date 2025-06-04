package com.mercadotech.productservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Marca a classe como uma fonte de definições de beans de configuração
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a configuração CORS a todos os caminhos da API
                .allowedOrigins("http://localhost:4200", "http://localhost:3000") // Permite requisições destes origens (endereço do seu frontend)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite os métodos HTTP especificados
                .allowedHeaders("*") // Permite todos os cabeçalhos
                .allowCredentials(true) // Permite o envio de cookies de credenciais
                .maxAge(3600); // Define por quanto tempo o navegador pode armazenar em cache os resultados da verificação de preflight (em segundos)
    }
}