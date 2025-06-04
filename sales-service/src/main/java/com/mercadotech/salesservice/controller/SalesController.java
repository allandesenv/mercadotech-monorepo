package com.mercadotech.salesservice.controller;

import com.mercadotech.salesservice.entity.Venda;
import com.mercadotech.salesservice.service.SalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController // Marca a classe como um controlador REST
@RequestMapping("/v1/vendas") // Define o caminho base para todos os endpoints deste controlador
@RequiredArgsConstructor // Gera construtor com argumentos obrigatórios (para injeção de dependências)
@Slf4j // Gera um logger para a classe
public class SalesController {

    private final SalesService salesService;

    /**
     * Endpoint para registrar uma nova venda.
     * Mapeia para POST /vendas
     * @param venda Objeto Venda contendo os detalhes da venda.
     * @return ResponseEntity com a Venda salva e status 201 Created.
     */
    @PostMapping
    public ResponseEntity<Venda> registrarVenda(@RequestBody Venda venda) {
        log.info("Recebida requisição para registrar venda: {}", venda);
        try {
            Venda novaVenda = salesService.registrarVenda(venda);
            log.info("Venda registrada com sucesso. ID: {}", novaVenda.getId());
            return new ResponseEntity<>(novaVenda, HttpStatus.CREATED); // Retorna 201 Created
        } catch (IllegalArgumentException e) {
            log.error("Erro ao registrar venda: {}", e.getMessage());
            // Isso pode acontecer se o stock-service retornar BAD_REQUEST (ex: saldo insuficiente)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ou falha na integração ao registrar venda: {}", e.getMessage());
            // Captura RuntimeException que pode vir do SalesService (rollback)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar a venda: " + e.getMessage());
        }
    }

    /**
     * Endpoint para gerar relatório de vendas por período.
     * Mapeia para GET /vendas?dataInicio=YYYY-MM-DDTHH:MM:SS&dataFim=YYYY-MM-DDTHH:MM:SS
     * @param dataInicio Data de início do período.
     * @param dataFim Data de fim do período.
     * @return Lista de vendas no período e status 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<Venda>> getVendasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        log.info("Recebida requisição para relatório de vendas entre {} e {}", dataInicio, dataFim);
        List<Venda> vendas = salesService.buscarVendasPorPeriodo(dataInicio, dataFim);
        log.info("Encontradas {} vendas no período.", vendas.size());
        return ResponseEntity.ok(vendas); // Retorna 200 OK
    }

    /**
     * Endpoint para obter histórico de vendas por produto.
     * Mapeia para GET /vendas/produto/{id}
     * @param produtoId ID do produto.
     * @return Histórico de vendas do produto e status 200 OK.
     */
    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<Venda>> getHistoricoVendasPorProduto(@PathVariable Long produtoId) {
        log.info("Recebida requisição para histórico de vendas do produto ID: {}", produtoId);
        List<Venda> historico = salesService.buscarHistoricoVendasPorProduto(produtoId);
        log.info("Encontradas {} vendas para o produto ID: {}", historico.size(), produtoId);
        return ResponseEntity.ok(historico); // Retorna 200 OK
    }
}