package com.mercadotech.salesservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data // Gera getters, setters, toString, equals e hashCode do Lombok
@NoArgsConstructor // Gera construtor sem argumentos do Lombok
@AllArgsConstructor // Gera construtor com todos os argumentos do Lombok
@Builder // Gera um builder para a classe
public class SaidaEstoqueDTO {
    private Long produtoId;
    private Integer quantidade;
    private LocalDateTime dataSaida;
    private String tipoSaida; // Usaremos String para o enum TipoSaida (VENDA)
    private String observacao;
}