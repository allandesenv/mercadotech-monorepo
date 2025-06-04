package com.mercadotech.validityservice.scheduler;

import com.mercadotech.validityservice.service.ValidityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled; // Importe esta anotação
import org.springframework.stereotype.Component; // Importe esta anotação

@Component // Marca a classe como um componente Spring, para ser escaneada
@RequiredArgsConstructor // Gera construtor com argumentos obrigatórios (para injeção de dependências)
@Slf4j // Gera um logger para a classe
public class ValidityCheckScheduler {

    private final ValidityService validityService;

    /**
     * Agenda a checagem diária de lotes vencidos para baixa automática.
     * A expressão cron é configurada em application.yml.
     * Exemplo: "0 0 0 * * ?" roda todo dia à meia-noite.
     */
    @Scheduled(cron = "${app.scheduler.cron-check-vencimento}")
    public void checkAndProcessExpiredLots() {
        log.info("Agendador: Executando checagem e baixa de lotes vencidos...");
        validityService.checarEBaixarLotesVencidos();
        log.info("Agendador: Checagem e baixa de lotes vencidos concluída.");
    }

    /**
     * Agenda a checagem diária de lotes próximos do vencimento para envio de alertas.
     * Roda 5 minutos após a checagem de vencidos, ou em outro horário definido.
     * Você pode ajustar o cron para evitar sobreposição ou otimizar recursos.
     * Exemplo: "0 5 0 * * ?" roda todo dia à 00:05.
     */
    @Scheduled(cron = "0 5 0 * * ?") // Hardcoded para 00:05 para este exemplo, mas poderia ser configurável
    public void checkAndAlertUpcomingLots() {
        log.info("Agendador: Executando checagem e alerta de lotes próximos do vencimento...");
        validityService.checarEAlertarLotesProximosVencimento();
        log.info("Agendador: Checagem e alerta de lotes próximos do vencimento concluída.");
    }

    // Você pode adicionar outros métodos agendados aqui, se necessário.
    // Ex: @Scheduled(fixedRate = 600000) // Roda a cada 10 minutos (600000 ms)
    // public void someOtherTask() {
    //     log.info("Executando outra tarefa a cada 10 minutos.");
    // }
}