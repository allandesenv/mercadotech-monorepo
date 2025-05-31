package com.mercadotech.stockservice.controller;

import com.mercadotech.stockservice.dto.ProdutoEstoqueDTO;
import com.mercadotech.stockservice.entity.EntradaEstoque;
import com.mercadotech.stockservice.entity.SaidaEstoque;
import com.mercadotech.stockservice.service.EstoqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController // Marca a classe como um controlador REST
@RequestMapping("/estoque") // Define o caminho base para todos os endpoints deste controlador
@RequiredArgsConstructor // Gera construtor com argumentos obrigatórios (para injeção de dependências)
@Slf4j // Gera um logger para a classe
public class EstoqueController {

    private final EstoqueService estoqueService;

    /**
     * Endpoint para registrar uma nova entrada de produtos no estoque.
     * Mapeia para POST /estoque/entrada
     * @param entradaEstoque Objeto EntradaEstoque contendo os detalhes da entrada.
     * @return ResponseEntity com a EntradaEstoque salva e status 201 Created.
     */
    @PostMapping("/entrada")
    public ResponseEntity<EntradaEstoque> registrarEntrada(@RequestBody EntradaEstoque entradaEstoque) {
        log.info("Recebida requisição para registrar entrada de estoque: {}", entradaEstoque);
        try {
            EntradaEstoque novaEntrada = estoqueService.registrarEntrada(entradaEstoque);
            log.info("Entrada de estoque registrada com sucesso. ID: {}", novaEntrada.getId());
            return new ResponseEntity<>(novaEntrada, HttpStatus.CREATED); // Retorna 201 Created
        } catch (IllegalArgumentException e) {
            log.error("Erro ao registrar entrada de estoque: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); // Retorna 400 Bad Request
        }
    }

    /**
     * Endpoint para registrar uma nova saída de produtos do estoque.
     * Mapeia para POST /estoque/saida
     * @param saidaEstoque Objeto SaidaEstoque contendo os detalhes da saída.
     * @return ResponseEntity com a SaidaEstoque salva e status 201 Created.
     */
    @PostMapping("/saida")
    public ResponseEntity<SaidaEstoque> registrarSaida(@RequestBody SaidaEstoque saidaEstoque) {
        log.info("Recebida requisição para registrar saída de estoque: {}", saidaEstoque);
        try {
            SaidaEstoque novaSaida = estoqueService.registrarSaida(saidaEstoque);
            log.info("Saída de estoque registrada com sucesso. ID: {}", novaSaida.getId());
            return new ResponseEntity<>(novaSaida, HttpStatus.CREATED); // Retorna 201 Created
        } catch (IllegalArgumentException e) {
            log.error("Erro ao registrar saída de estoque: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); // Retorna 400 Bad Request
        }
    }

    /**
     * Endpoint para obter o saldo atual de um produto.
     * Mapeia para GET /estoque/{produtoId}
     * @param produtoId ID do produto.
     * @return ResponseEntity com o saldo atual e status 200 OK.
     * @throws ResponseStatusException Se o produto não for encontrado.
     */
    @GetMapping("/{produtoId}")
    public ResponseEntity<Integer> getSaldoAtual(@PathVariable Long produtoId) {
        log.info("Recebida requisição para obter saldo do produto com ID: {}", produtoId);
        try {
            // Primeiro, verificar se o produto existe no product-service
            ProdutoEstoqueDTO produto = estoqueService.getProdutoById(produtoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto com ID " + produtoId + " não encontrado."));

            Integer saldo = estoqueService.calcularSaldoAtual(produtoId);
            log.info("Saldo atual do produto {} ({}): {}", produto.getName(), produto.getId(), saldo);
            return ResponseEntity.ok(saldo); // Retorna 200 OK
        } catch (ResponseStatusException e) {
            // Re-lança a exceção para que o Spring Boot a capture e retorne o status adequado
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao obter saldo para o produto {}: {}", produtoId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a requisição.");
        }
    }
}