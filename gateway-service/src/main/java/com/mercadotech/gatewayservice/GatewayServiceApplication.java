package com.mercadotech.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // << ESSA IMPORTAÇÃO É NECESSÁRIA >>
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration; // << ESSA IMPORTAÇÃO É NECESSÁRIA >>


@SpringBootApplication
@ComponentScan(basePackages = "com.mercadotech.gatewayservice") // Linha opcional, pode manter ou remover
@Configuration // << ANOTAÇÃO PARA DECLARAR BEANS EXPLÍCITOS >>
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

    /**
     * Declara explicitamente o AuthorizationFilterFactory como um bean do Spring.
     * Isso garante que o Gateway o descubra e o registre.
     */
    @Bean
    public AuthorizeGatewayFilterFactory authorizationFilterFactory() {
        return new AuthorizeGatewayFilterFactory();
    }
}