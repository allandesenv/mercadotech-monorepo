package com.mercadotech.stockservice.client;

import com.mercadotech.stockservice.dto.ProdutoEstoqueDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "product-service", url = "${product-service.url:http://localhost:8081}") // Nome do serviço no Eureka e URL de fallback
public interface ProductServiceFeignClient {

    @GetMapping("/products/{id}") // Endpoint do product-service para buscar produto por ID
    Optional<ProdutoEstoqueDTO> getProductById(@PathVariable("id") Long id);
}