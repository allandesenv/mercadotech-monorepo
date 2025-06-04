package com.mercadotech.validityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaidaEstoqueDTO {
    private Long produtoId;
    private Integer quantidade;
    private LocalDateTime dataSaida;
    private String tipoSaida; // Ex: "PERDA", "VENCIMENTO"
    private String observacao;
}