package com.mercadotech.stockservice.service;

import com.mercadotech.stockservice.client.ProductServiceFeignClient;
import com.mercadotech.stockservice.dto.ProdutoEstoqueDTO;
import com.mercadotech.stockservice.entity.EntradaEstoque;
import com.mercadotech.stockservice.entity.SaidaEstoque;
import com.mercadotech.stockservice.enums.TipoSaida;
import com.mercadotech.stockservice.repository.EntradaEstoqueRepository;
import com.mercadotech.stockservice.repository.SaidaEstoqueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service // Marca a classe como um componente de serviço Spring
@RequiredArgsConstructor // Gera construtor com argumentos obrigatórios (para injeção de dependências via final)
@Slf4j // Gera um logger para a classe
public class EstoqueService {

    private final EntradaEstoqueRepository entradaEstoqueRepository;
    private final SaidaEstoqueRepository saidaEstoqueRepository;
    private final ProductServiceFeignClient productServiceFeignClient;

    /**
     * Registra uma entrada de produto no estoque.
     * @param entradaEstoque Objeto EntradaEstoque contendo os detalhes da entrada.
     * @return EntradaEstoque O objeto EntradaEstoque salvo.
     * @throws IllegalArgumentException Se o produto não for encontrado.
     */
    @Transactional // Garante que a operação seja atômica
    public EntradaEstoque registrarEntrada(EntradaEstoque entradaEstoque) {
        // 1. Validar se o produto existe no product-service
        Long produtoId = entradaEstoque.getProdutoId();
        log.info("Verificando a existência do produto com ID: {}", produtoId);
        Optional<ProdutoEstoqueDTO> produtoOptional = productServiceFeignClient.getProductById(produtoId);

        if (produtoOptional.isEmpty()) {
            log.error("Produto com ID {} não encontrado no product-service.", produtoId);
            throw new IllegalArgumentException("Produto com ID " + produtoId + " não encontrado.");
        }

        // Produto encontrado, pode prosseguir
        ProdutoEstoqueDTO produto = produtoOptional.get();
        log.info("Produto encontrado: {} - {}", produto.getId(), produto.getName());

        // 2. Preencher a data de entrada se não for fornecida
        if (entradaEstoque.getDataEntrada() == null) {
            entradaEstoque.setDataEntrada(LocalDateTime.now());
        }

        // 3. Salvar a entrada no banco de dados
        log.info("Registrando entrada de {} unidades do produto {}", entradaEstoque.getQuantidade(), produto.getName());
        return entradaEstoqueRepository.save(entradaEstoque);
    }

    /**
     * Registra uma saída de produto do estoque.
     * Realiza validação de saldo antes de registrar a saída.
     * @param saidaEstoque Objeto SaidaEstoque contendo os detalhes da saída.
     * @return SaidaEstoque O objeto SaidaEstoque salvo.
     * @throws IllegalArgumentException Se o produto não for encontrado ou se o saldo for insuficiente.
     */
    @Transactional // Garante que a operação seja atômica
    public SaidaEstoque registrarSaida(SaidaEstoque saidaEstoque) {
        // 1. Validar se o produto existe no product-service
        Long produtoId = saidaEstoque.getProdutoId();
        log.info("Verificando a existência do produto com ID: {}", produtoId);
        Optional<ProdutoEstoqueDTO> produtoOptional = productServiceFeignClient.getProductById(produtoId);

        if (produtoOptional.isEmpty()) {
            log.error("Produto com ID {} não encontrado no product-service.", produtoId);
            throw new IllegalArgumentException("Produto com ID " + produtoId + " não encontrado.");
        }

        ProdutoEstoqueDTO produto = produtoOptional.get();
        log.info("Produto encontrado: {} - {}", produto.getId(), produto.getName());

        // 2. Calcular o saldo atual do produto
        Integer saldoAtual = calcularSaldoAtual(produtoId);
        log.info("Saldo atual do produto {}: {}", produto.getName(), saldoAtual);

        // 3. Validar se há quantidade suficiente em estoque
        if (saldoAtual < saidaEstoque.getQuantidade()) {
            log.error("Saldo insuficiente para o produto {}. Saldo atual: {}, Quantidade de saída: {}",
                    produto.getName(), saldoAtual, saidaEstoque.getQuantidade());
            throw new IllegalArgumentException("Saldo insuficiente para o produto " + produto.getName() + " (ID: " + produtoId + "). Saldo atual: " + saldoAtual + ", Tentativa de saída: " + saidaEstoque.getQuantidade());
        }

        // 4. Preencher a data de saída se não for fornecida
        if (saidaEstoque.getDataSaida() == null) {
            saidaEstoque.setDataSaida(LocalDateTime.now());
        }

        // 5. Salvar a saída no banco de dados
        log.info("Registrando saída de {} unidades do produto {} (Tipo: {})", saidaEstoque.getQuantidade(), produto.getName(), saidaEstoque.getTipoSaida());
        return saidaEstoqueRepository.save(saidaEstoque);
    }

    /**
     * Calcula o saldo atual de um produto com base em todas as entradas e saídas.
     * @param produtoId ID do produto.
     * @return O saldo atual do produto (quantidade em estoque).
     * @throws IllegalArgumentException Se o produto não for encontrado (embora o método principal já valide).
     */
    public Integer calcularSaldoAtual(Long produtoId) {
        log.info("Calculando saldo para o produto ID: {}", produtoId);
        // Não é necessário validar o produto aqui novamente se o método for chamado por registrarEntrada/Saida,
        // mas pode ser útil se for um método público exposto.
        // Optional<ProdutoEstoqueDTO> produtoOptional = productServiceFeignClient.getProductById(produtoId);
        // if (produtoOptional.isEmpty()) {
        //     throw new IllegalArgumentException("Produto com ID " + produtoId + " não encontrado para cálculo de saldo.");
        // }

        List<EntradaEstoque> entradas = entradaEstoqueRepository.findByProdutoIdOrderByDataEntradaAsc(produtoId);
        List<SaidaEstoque> saidas = saidaEstoqueRepository.findByProdutoIdOrderByDataSaidaAsc(produtoId);

        int totalEntradas = entradas.stream()
                .mapToInt(EntradaEstoque::getQuantidade)
                .sum();

        int totalSaidas = saidas.stream()
                .mapToInt(SaidaEstoque::getQuantidade)
                .sum();

        int saldo = totalEntradas - totalSaidas;
        log.info("Saldo calculado para produto ID {}: {} (Entradas: {}, Saídas: {})", produtoId, saldo, totalEntradas, totalSaidas);
        return saldo;
    }

    /**
     * Busca um produto por ID no product-service.
     * Este método é auxiliar e pode ser usado por outros métodos que precisam dos dados do produto.
     * @param produtoId ID do produto.
     * @return Um Optional contendo o ProdutoEstoqueDTO se encontrado, ou vazio.
     */
    public Optional<ProdutoEstoqueDTO> getProdutoById(Long produtoId) {
        return productServiceFeignClient.getProductById(produtoId);
    }
}