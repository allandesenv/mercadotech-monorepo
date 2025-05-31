package com.mercadotech.salesservice.client;

import com.mercadotech.salesservice.dto.SaidaEstoqueDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity; // Importar ResponseEntity

@FeignClient(name = "stock-service", url = "${stock-service.url:http://localhost:8084}") // Nome do serviço no Eureka e URL de fallback
public interface StockServiceFeignClient {

    /**
     * Endpoint para registrar uma saída (baixa) de estoque no stock-service.
     * Mapeia para POST /estoque/saida no stock-service.
     * @param saidaEstoqueDTO DTO com os detalhes da saída de estoque.
     * @return ResponseEntity da resposta do stock-service.
     */
    @PostMapping("/estoque/saida")
    ResponseEntity<Void> registrarSaida(@RequestBody SaidaEstoqueDTO saidaEstoqueDTO);
    // Usamos ResponseEntity<Void> pois não precisamos do corpo da resposta, apenas do status
}