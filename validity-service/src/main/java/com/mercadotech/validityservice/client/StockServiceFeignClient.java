package com.mercadotech.validityservice.client;

import com.mercadotech.validityservice.dto.SaidaEstoqueDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "stock-service", url = "${stock-service.url:http://localhost:8084}")
public interface StockServiceFeignClient {

    /**
     * Aciona uma baixa de estoque no stock-service.
     * @param saidaEstoqueDTO DTO com os detalhes da baixa.
     * @return ResponseEntity da operação no stock-service.
     */
    @PostMapping("/estoque/saida")
    ResponseEntity<Void> registrarSaida(@RequestBody SaidaEstoqueDTO saidaEstoqueDTO);
}