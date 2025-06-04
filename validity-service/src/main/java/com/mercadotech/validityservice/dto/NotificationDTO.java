package com.mercadotech.validityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private String recipient; // Ex: email, userId
    private String subject; // Assunto da notificação
    private String message; // Conteúdo da notificação
    private String type; // Ex: "EMAIL", "SMS", "IN_APP"
}