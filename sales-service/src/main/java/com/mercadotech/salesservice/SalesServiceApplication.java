package com.mercadotech.salesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient // Habilita o serviço a se registrar no Eureka Server
@EnableFeignClients(basePackages = "com.mercadotech.salesservice.client") // Habilita o uso de Feign Clients e especifica o pacote
@ComponentScan(basePackages = {"com.mercadotech.salesservice"}) // Garante que o Spring escaneie este pacote
public class SalesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesServiceApplication.class, args);
    }

}