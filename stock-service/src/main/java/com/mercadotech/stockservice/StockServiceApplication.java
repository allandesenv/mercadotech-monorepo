package com.mercadotech.stockservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient // Habilita o serviço a se registrar no Eureka Server
@EnableFeignClients(basePackages = "com.mercadotech.stockservice.client") // Habilita o uso de Feign Clients e especifica o pacote
@ComponentScan(basePackages = {"com.mercadotech.stockservice"}) // Garante que o Spring escaneie este pacote
public class StockServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockServiceApplication.class, args);
    }

}