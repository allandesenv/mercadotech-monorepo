package com.mercadotech.validityservice.client;

import com.mercadotech.validityservice.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${notification-service.url:http://localhost:8087}")
public interface NotificationServiceFeignClient {

    /**
     * Envia uma notificação para o notification-service.
     * @param notificationDTO DTO com os detalhes da notificação.
     * @return ResponseEntity da operação no notification-service.
     */
    @PostMapping("/notifications") // Assumindo que o endpoint de notificação é /notifications
    ResponseEntity<Void> sendNotification(@RequestBody NotificationDTO notificationDTO);
}