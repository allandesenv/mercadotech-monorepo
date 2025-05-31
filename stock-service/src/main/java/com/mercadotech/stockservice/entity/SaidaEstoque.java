package com.mercadotech.stockservice.entity;

import com.mercadotech.stockservice.enums.TipoSaida;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "saidas_estoque") // Nome da tabela no banco de dados
@Data // Gera getters, setters, toString, equals e hashCode do Lombok
@NoArgsConstructor // Gera construtor sem argumentos do Lombok
@AllArgsConstructor // Gera construtor com todos os argumentos do Lombok
@Builder // Gera um builder para a classe (útil para criar objetos de forma fluente)
public class SaidaEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Geração automática do ID
    private Long id;

    @Column(nullable = false)
    private Long produtoId; // ID do produto que está saindo do estoque

    @Column(nullable = false)
    private Integer quantidade; // Quantidade de produtos que saíram

    @Column(nullable = false)
    private LocalDateTime dataSaida; // Data e hora da saída

    @Enumerated(EnumType.STRING) // Armazena o enum como String no banco de dados
    @Column(nullable = false)
    private TipoSaida tipoSaida; // Tipo de saída (venda, perda, consumo, etc.)

    @Column(length = 255)
    private String observacao; // Observações sobre a saída
}