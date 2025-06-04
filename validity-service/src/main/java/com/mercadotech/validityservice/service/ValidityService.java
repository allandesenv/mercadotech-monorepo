package com.mercadotech.validityservice.service;

import com.mercadotech.validityservice.client.NotificationServiceFeignClient;
import com.mercadotech.validityservice.client.StockServiceFeignClient;
import com.mercadotech.validityservice.dto.NotificationDTO;
import com.mercadotech.validityservice.dto.SaidaEstoqueDTO;
import com.mercadotech.validityservice.entity.LoteValidade;
import com.mercadotech.validityservice.enums.LoteStatus;
import com.mercadotech.validityservice.repository.LoteValidadeRepository;
import jakarta.transaction.Transactional; // Importe jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus; // Importar HttpStatus
import org.springframework.http.ResponseEntity; // Importar ResponseEntity
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service // Marca a classe como um componente de serviço Spring
@RequiredArgsConstructor // Gera construtor com argumentos obrigatórios (para injeção de dependências via final)
@Slf4j // Gera um logger para a classe
public class ValidityService {

    private final LoteValidadeRepository loteValidadeRepository;
    private final StockServiceFeignClient stockServiceFeignClient;
    private final NotificationServiceFeignClient notificationServiceFeignClient;

    @Value("${app.scheduler.dias-alerta-vencimento}")
    private int diasParaAlertaVencimento;

    /**
     * Registra um novo lote de validade.
     * @param lote LoteValidade a ser registrado.
     * @return LoteValidade registrado.
     */
    @Transactional
    public LoteValidade registrarLote(LoteValidade lote) {
        log.info("Registrando novo lote de validade para produto ID: {}", lote.getProdutoId());
        // Garante que a data de registro seja preenchida se não vier
        if (lote.getDataRegistro() == null) {
            lote.setDataRegistro(LocalDateTime.now());
        }
        // Lotes recém-registrados são ATIVOS por padrão
        if (lote.getStatus() == null) {
            lote.setStatus(LoteStatus.ATIVO);
        }
        return loteValidadeRepository.save(lote);
    }

    /**
     * Busca lotes que estão vencendo nos próximos X dias ou que já venceram e ainda estão ATIVOS.
     * @param dias Quantidade de dias para verificar a frente (ex: 7 para próximos 7 dias).
     * @return Lista de LoteValidade que se encaixam nos critérios.
     */
    public List<LoteValidade> getLotesVencendo(int dias) {
        LocalDate dataLimite = LocalDate.now().plusDays(dias);
        log.info("Buscando lotes ativos vencendo em ou antes de: {}", dataLimite);
        return loteValidadeRepository.findByStatusAndDataValidadeLessThanEqual(LoteStatus.ATIVO, dataLimite);
    }

    /**
     * Processa a perda de um lote de produtos (manual ou automática por vencimento).
     * Dá baixa no estoque e atualiza o status do lote.
     * @param loteId ID do lote a ser processado como perda.
     * @param tipoPerda Tipo de saída no estoque (ex: "PERDA", "VENCIMENTO").
     * @param observacao Observação para a baixa.
     * @return LoteValidade atualizado com status BAIXADO.
     * @throws IllegalArgumentException se o lote não for encontrado, não for ATIVO ou a baixa falhar.
     */
    @Transactional // Garante atomicidade: baixa no estoque E atualização do lote
    public LoteValidade processarPerdaLote(Long loteId, String tipoPerda, String observacao) {
        log.info("Processando perda para lote ID: {}", loteId);
        LoteValidade lote = loteValidadeRepository.findById(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote de validade com ID " + loteId + " não encontrado."));

        if (lote.getStatus() != LoteStatus.ATIVO && lote.getStatus() != LoteStatus.PENDENTE_BAIXA) { // Permite processar perda de BAIXADO (evitar reprocessamento)
            throw new IllegalArgumentException("Lote com ID " + loteId + " não está no status ATIVO ou PENDENTE_BAIXA para processar perda.");
        }

        // 1. Acionar baixa no stock-service
        SaidaEstoqueDTO saidaEstoque = SaidaEstoqueDTO.builder()
                .produtoId(lote.getProdutoId())
                .quantidade(lote.getQuantidade())
                .dataSaida(LocalDateTime.now())
                .tipoSaida(tipoPerda)
                .observacao("Baixa de lote de validade ID " + lote.getId() + " - " + observacao)
                .build();

        try {
            log.info("Solicitando baixa de {} unidades do produto ID {} no stock-service.", lote.getQuantidade(), lote.getProdutoId());
            ResponseEntity<Void> response = stockServiceFeignClient.registrarSaida(saidaEstoque);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Falha ao baixar estoque para lote ID {}. Status do stock-service: {}. Mensagem: {}", lote.getId(), response.getStatusCode(), response.getBody());
                throw new RuntimeException("Falha na baixa de estoque para lote " + loteId + ". Status do stock-service: " + response.getStatusCode());
            }
            log.info("Baixa de estoque para lote ID {} registrada com sucesso.", lote.getId());
        } catch (Exception e) {
            log.error("Erro de comunicação com stock-service ao processar perda para lote ID {}. Forçando rollback. Erro: {}", lote.getId(), e.getMessage());
            throw new RuntimeException("Erro na comunicação com stock-service: " + e.getMessage(), e);
        }

        // 2. Atualizar status do lote para BAIXADO
        lote.setStatus(LoteStatus.BAIXADO);
        lote.setObservacao(lote.getObservacao() + " | Baixado em " + LocalDateTime.now() + " como " + tipoPerda);
        return loteValidadeRepository.save(lote);
    }

    /**
     * Checa lotes vencidos e aciona a baixa automática. Chamado pelo agendador.
     */
    @Transactional // Garante que a checagem e atualização/baixa sejam atômicas
    public void checarEBaixarLotesVencidos() {
        log.info("Iniciando checagem diária de lotes vencidos...");
        LocalDate hoje = LocalDate.now();
        List<LoteValidade> lotesVencidosAtivos = loteValidadeRepository.findByStatusAndDataValidadeLessThanEqual(LoteStatus.ATIVO, hoje);

        if (lotesVencidosAtivos.isEmpty()) {
            log.info("Nenhum lote ATIVO vencido encontrado para hoje.");
            return;
        }

        log.info("Encontrados {} lotes ATIVOS vencidos. Processando baixas...", lotesVencidosAtivos.size());
        for (LoteValidade lote : lotesVencidosAtivos) {
            try {
                // Marcar como VENCIDO antes de tentar a baixa para distinguir
                lote.setStatus(LoteStatus.VENCIDO); // Temporariamente muda para VENCIDO
                loteValidadeRepository.save(lote); // Salva o status VENCIDO

                // Tenta processar a perda, que também atualiza para BAIXADO
                processarPerdaLote(lote.getId(), "VENCIMENTO_AUTOMATICO", "Produto vencido automaticamente em " + hoje);
                log.info("Lote ID {} (Produto ID: {}) baixado por vencimento.", lote.getId(), lote.getProdutoId());

            } catch (Exception e) {
                log.error("Falha ao processar baixa automática para lote ID {} (Produto ID: {}). Erro: {}",
                        lote.getId(), lote.getProdutoId(), e.getMessage());
                // O status do lote permanecerá VENCIDO se a baixa falhar, para reprocessamento manual/futuro.
                // Notificar sobre esta falha também seria importante aqui.
            }
        }
        log.info("Checagem diária de lotes vencidos concluída.");
    }

    /**
     * Checa lotes próximos do vencimento e envia alertas. Chamado pelo agendador.
     */
    public void checarEAlertarLotesProximosVencimento() {
        log.info("Iniciando checagem diária de lotes próximos do vencimento (em {} dias)...", diasParaAlertaVencimento);
        LocalDate dataAlerta = LocalDate.now().plusDays(diasParaAlertaVencimento);
        List<LoteValidade> lotesProximosVencimento = loteValidadeRepository.findByStatusAndDataValidadeLessThanEqual(LoteStatus.ATIVO, dataAlerta);

        // Filtra para remover os que já venceram, pois esses são tratados por 'checarEBaixarLotesVencidos'
        lotesProximosVencimento.removeIf(lote -> lote.getDataValidade().isBefore(LocalDate.now()));

        if (lotesProximosVencimento.isEmpty()) {
            log.info("Nenhum lote ATIVO próximo do vencimento encontrado.");
            return;
        }

        log.info("Encontrados {} lotes ATIVOS próximos do vencimento. Enviando alertas...", lotesProximosVencimento.size());
        for (LoteValidade lote : lotesProximosVencimento) {
            String mensagem = String.format("ALERTA DE VENCIMENTO: O lote ID %d do produto ID %d (%s unidades) vencerá em %s. Status: %s.",
                    lote.getId(), lote.getProdutoId(), lote.getQuantidade(), lote.getDataValidade(), lote.getStatus());
            NotificationDTO notification = NotificationDTO.builder()
                    .recipient("gerente@mercadotech.com.br") // Exemplo: E-mail do gerente
                    .subject("ALERTA: Produto Proximo do Vencimento - Lote " + lote.getId())
                    .message(mensagem)
                    .type("EMAIL") // Tipo de notificação
                    .build();

            try {
                ResponseEntity<Void> response = notificationServiceFeignClient.sendNotification(notification);
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Alerta enviado com sucesso para lote ID {}.", lote.getId());
                } else {
                    log.error("Falha ao enviar alerta para lote ID {}. Status: {}. Mensagem: {}", lote.getId(), response.getStatusCode(), response.getBody());
                }
            } catch (Exception e) {
                log.error("Erro de comunicação com notification-service ao enviar alerta para lote ID {}. Erro: {}", lote.getId(), e.getMessage());
            }
        }
        log.info("Checagem de lotes próximos do vencimento concluída.");
    }
}