package com.mercadotech.salesservice.service;

import com.mercadotech.salesservice.client.StockServiceFeignClient;
import com.mercadotech.salesservice.dto.SaidaEstoqueDTO;
import com.mercadotech.salesservice.entity.Venda;
import com.mercadotech.salesservice.repository.VendaRepository;
import jakarta.transaction.Transactional; // Importe jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity; // Importar ResponseEntity

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service // Marca a classe como um componente de serviço Spring
@RequiredArgsConstructor // Gera construtor com argumentos obrigatórios (para injeção de dependências via final)
@Slf4j // Gera um logger para a classe
public class SalesService {

    private final VendaRepository vendaRepository;
    private final StockServiceFeignClient stockServiceFeignClient;

    /**
     * Registra uma nova venda e aciona a baixa automática no stock-service.
     * Implementa lógica de rollback em caso de falha na baixa de estoque.
     * @param venda Objeto Venda contendo os detalhes da venda.
     * @return Venda O objeto Venda salvo.
     * @throws RuntimeException Se a baixa no estoque falhar, forçando um rollback.
     */
    @Transactional // Garante que a operação de venda e baixa de estoque seja atômica
    public Venda registrarVenda(Venda venda) {
        log.info("Iniciando registro de venda para produto ID: {}, quantidade: {}", venda.getProdutoId(), venda.getQuantidade());

        // 1. Calcular valor total da venda
        if (venda.getValorUnitario() == null) {
            // Em um cenário real, você buscaria o preço do produto no product-service
            // Para simplificar, estamos assumindo que o valorUnitario é fornecido ou pode ser um valor padrão
            log.warn("Valor unitário não fornecido para venda do produto ID: {}. Ajuste na lógica para buscar do ProductService.", venda.getProdutoId());
            venda.setValorUnitario(BigDecimal.ZERO); // Ou lance uma exceção
        }
        BigDecimal valorTotal = venda.getValorUnitario().multiply(BigDecimal.valueOf(venda.getQuantidade()));
        venda.setValorTotal(valorTotal);
        log.info("Valor total da venda calculado: {}", valorTotal);

        // 2. Preencher a data da venda se não for fornecida
        if (venda.getDataVenda() == null) {
            venda.setDataVenda(LocalDateTime.now());
        }

        // 3. Salvar a venda no banco de dados do sales-service (primeira parte da transação)
        Venda novaVenda = vendaRepository.save(venda);
        log.info("Venda registrada no sales-service com ID: {}", novaVenda.getId());

        // 4. Acionar baixa de estoque no stock-service
        SaidaEstoqueDTO saidaEstoqueDTO = SaidaEstoqueDTO.builder()
                .produtoId(novaVenda.getProdutoId())
                .quantidade(novaVenda.getQuantidade())
                .dataSaida(LocalDateTime.now())
                .tipoSaida("VENDA") // Tipo de saída para uma venda
                .observacao("Baixa automática via sales-service para Venda ID: " + novaVenda.getId())
                .build();

        log.info("Acionando baixa de estoque no stock-service para produto ID: {}, quantidade: {}", saidaEstoqueDTO.getProdutoId(), saidaEstoqueDTO.getQuantidade());
        try {
            ResponseEntity<Void> response = stockServiceFeignClient.registrarSaida(saidaEstoqueDTO);

            if (!response.getStatusCode().is2xxSuccessful()) {
                // Se o stock-service não retornar sucesso (ex: 400 Bad Request por saldo insuficiente)
                log.error("Falha ao registrar baixa de estoque no stock-service. Status: {}. Mensagem: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Falha na baixa de estoque. Status do stock-service: " + response.getStatusCode());
            }
            log.info("Baixa de estoque registrada com sucesso no stock-service.");
        } catch (Exception e) {
            // Captura qualquer exceção (ex: FeignException, ConnectException) e força o rollback
            log.error("Erro de comunicação ou lógica com o stock-service ao tentar baixar estoque. Forçando rollback da venda. Erro: {}", e.getMessage());
            // Uma RuntimeException causará o rollback da transação @Transactional automaticamente
            throw new RuntimeException("Erro ao processar baixa de estoque: " + e.getMessage(), e);
        }

        return novaVenda;
    }

    /**
     * Busca vendas em um determinado período.
     * @param dataInicio Data de início do período.
     * @param dataFim Data de fim do período.
     * @return Lista de vendas no período especificado.
     */
    public List<Venda> buscarVendasPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        log.info("Buscando vendas entre {} e {}", dataInicio, dataFim);
        return vendaRepository.findByDataVendaBetween(dataInicio, dataFim);
    }

    /**
     * Busca o histórico de vendas para um produto específico.
     * @param produtoId ID do produto.
     * @return Lista de vendas para o produto.
     */
    public List<Venda> buscarHistoricoVendasPorProduto(Long produtoId) {
        log.info("Buscando histórico de vendas para o produto ID: {}", produtoId);
        return vendaRepository.findByProdutoId(produtoId);
    }
}