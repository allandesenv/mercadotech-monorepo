package com.mercadotech.validityservice.entity;

import com.mercadotech.validityservice.enums.LoteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate; // Usaremos LocalDate para a data (apenas dia, mês, ano)
import java.time.LocalDateTime; // Para data e hora de registro

@Entity
@Table(name = "lotes_validade") // Nome da tabela no banco de dados
@Data // Gera getters, setters, toString, equals e hashCode do Lombok
@NoArgsConstructor // Gera construtor sem argumentos do Lombok
@AllArgsConstructor // Gera construtor com todos os argumentos do Lombok
@Builder // Gera um builder para a classe (útil para criar objetos de forma fluente)
public class LoteValidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Geração automática do ID
    private Long id;

    @Column(nullable = false)
    private Long produtoId; // ID do produto ao qual este lote de validade se refere

    @Column(nullable = false)
    private LocalDate dataEntradaLote; // Data em que este lote específico entrou no sistema/estoque

    @Column(nullable = false)
    private LocalDate dataValidade; // Data de validade do produto neste lote

    @Column(nullable = false)
    private Integer quantidade; // Quantidade de itens neste lote

    // Status do lote (Ativo, Vencido, Baixado/Perdido)
    @Enumerated(EnumType.STRING) // Armazena o enum como String no banco de dados
    @Column(nullable = false)
    private LoteStatus status;

    @Column(nullable = false)
    private LocalDateTime dataRegistro; // Data e hora do registro deste lote no validity-service

    @Column(length = 500)
    private String observacao; // Observações sobre o lote
}