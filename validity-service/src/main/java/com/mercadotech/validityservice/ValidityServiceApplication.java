package com.mercadotech.validityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling; // Importe esta anotação para o agendador

@SpringBootApplication
@EnableDiscoveryClient // Habilita o serviço a se registrar no Eureka Server
@EnableFeignClients(basePackages = {"com.mercadotech.validityservice.client"}) // Habilita o uso de Feign Clients
@ComponentScan(basePackages = {"com.mercadotech.validityservice"}) // Garante que o Spring escaneie este pacote
@EnableScheduling // Habilita o agendador de tarefas do Spring
public class ValidityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValidityServiceApplication.class, args);
    }

}