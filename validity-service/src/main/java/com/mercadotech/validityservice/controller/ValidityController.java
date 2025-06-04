package com.mercadotech.validityservice.controller;

import com.mercadotech.validityservice.entity.LoteValidade;
import com.mercadotech.validityservice.service.ValidityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController // Marca a classe como um controlador REST
@RequestMapping("/validade") // Define o caminho base para todos os endpoints deste controlador
@RequiredArgsConstructor // Gera construtor com argumentos obrigatórios (para injeção de dependências)
@Slf4j // Gera um logger para a classe
public class ValidityController {

    private final ValidityService validityService;

    /**
     * Endpoint para registrar um novo lote de validade.
     * Mapeia para POST /validade/registro
     * @param lote LoteValidade a ser registrado.
     * @return ResponseEntity com o LoteValidade salvo e status 201 Created.
     */
    @PostMapping("/registro")
    public ResponseEntity<LoteValidade> registrarLote(@RequestBody LoteValidade lote) {
        log.info("Recebida requisição para registrar lote de validade: {}", lote);
        try {
            LoteValidade novoLote = validityService.registrarLote(lote);
            log.info("Lote de validade registrado com sucesso. ID: {}", novoLote.getId());
            return new ResponseEntity<>(novoLote, HttpStatus.CREATED); // Retorna 201 Created
        } catch (IllegalArgumentException e) {
            log.error("Erro ao registrar lote de validade: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao registrar lote de validade: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar o registro do lote.");
        }
    }

    /**
     * Endpoint para consultar produtos vencendo em X dias.
     * Mapeia para GET /validade/vencendo?dias=7
     * @param dias Quantidade de dias para verificar a frente.
     * @return Lista de lotes vencendo e status 200 OK.
     */
    @GetMapping("/vencendo")
    public ResponseEntity<List<LoteValidade>> getLotesVencendo(@RequestParam(defaultValue = "7") int dias) {
        log.info("Recebida requisição para buscar lotes vencendo nos próximos {} dias.", dias);
        List<LoteValidade> lotes = validityService.getLotesVencendo(dias);
        log.info("Encontrados {} lotes vencendo ou vencidos nos próximos {} dias.", lotes.size(), dias);
        return ResponseEntity.ok(lotes); // Retorna 200 OK
    }

    /**
     * Endpoint para gerar perda manual de um lote.
     * Mapeia para POST /validade/perda
     * @param loteId ID do lote a ser marcado como perda.
     * @param observacao Observação sobre a perda.
     * @return ResponseEntity com o LoteValidade atualizado e status 200 OK.
     */
    @PostMapping("/perda")
    public ResponseEntity<LoteValidade> registrarPerdaManual(
            @RequestParam Long loteId,
            @RequestParam(required = false) String observacao) {
        log.info("Recebida requisição para registrar perda manual para lote ID: {}", loteId);
        try {
            LoteValidade lotePerdido = validityService.processarPerdaLote(loteId, "PERDA_MANUAL", observacao);
            log.info("Perda manual para lote ID {} registrada com sucesso.", loteId);
            return ResponseEntity.ok(lotePerdido); // Retorna 200 OK
        } catch (IllegalArgumentException e) {
            log.error("Erro ao registrar perda manual para lote ID {}: {}", loteId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Erro inesperado ou falha na integração ao registrar perda manual para lote ID {}: {}", loteId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar perda manual: " + e.getMessage());
        }
    }

    // Opcional: Endpoint para acionar manualmente a checagem de vencidos (para testes)
    @PostMapping("/checar-vencidos-manual")
    public ResponseEntity<String> acionarChecagemVencidosManual() {
        log.info("Recebida requisição para acionar checagem manual de lotes vencidos.");
        validityService.checarEBaixarLotesVencidos();
        return ResponseEntity.ok("Checagem manual de lotes vencidos acionada. Verifique os logs do serviço.");
    }

    // Opcional: Endpoint para acionar manualmente a checagem de alertas (para testes)
    @PostMapping("/checar-alertas-manual")
    public ResponseEntity<String> acionarChecagemAlertasManual() {
        log.info("Recebida requisição para acionar checagem manual de alertas de vencimento.");
        validityService.checarEAlertarLotesProximosVencimento();
        return ResponseEntity.ok("Checagem manual de alertas de vencimento acionada. Verifique os logs do serviço.");
    }
}