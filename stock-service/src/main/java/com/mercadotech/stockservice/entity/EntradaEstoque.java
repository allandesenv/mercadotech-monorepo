package com.mercadotech.stockservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "entradas_estoque") // Nome da tabela no banco de dados
@Data // Gera getters, setters, toString, equals e hashCode do Lombok
@NoArgsConstructor // Gera construtor sem argumentos do Lombok
@AllArgsConstructor // Gera construtor com todos os argumentos do Lombok
@Builder // Gera um builder para a classe (útil para criar objetos de forma fluente)
public class EntradaEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Geração automática do ID
    private Long id;

    @Column(nullable = false)
    private Long produtoId; // ID do produto que está entrando em estoque

    @Column(nullable = false)
    private Integer quantidade; // Quantidade de produtos que entraram

    @Column(nullable = false, precision = 10, scale = 2) // Precisão e escala para valores monetários
    private BigDecimal custoUnitario; // Custo unitário do produto na entrada

    @Column(nullable = false)
    private LocalDateTime dataEntrada; // Data e hora da entrada

    @Column(length = 255)
    private String observacao; // Observações sobre a entrada
}