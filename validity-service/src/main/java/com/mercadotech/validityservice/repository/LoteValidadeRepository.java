package com.mercadotech.validityservice.repository;

import com.mercadotech.validityservice.entity.LoteValidade;
import com.mercadotech.validityservice.enums.LoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository // Marca a interface como um componente de repositório Spring
public interface LoteValidadeRepository extends JpaRepository<LoteValidade, Long> {
    // Spring Data JPA fornece automaticamente métodos CRUD (save, findById, findAll, delete, etc.)

    /**
     * Busca lotes por produto e status.
     * @param produtoId ID do produto.
     * @param status Status do lote.
     * @return Lista de lotes que correspondem aos critérios.
     */
    List<LoteValidade> findByProdutoIdAndStatus(Long produtoId, LoteStatus status);

    /**
     * Busca lotes que estão ativos e vencendo em ou antes de uma determinada data.
     * @param dataLimite Data limite para vencimento (hoje + X dias).
     * @param status Status do lote (ATIVO).
     * @return Lista de lotes vencendo ou já vencidos que ainda estão ativos.
     */
    List<LoteValidade> findByStatusAndDataValidadeLessThanEqual(LoteStatus status, LocalDate dataLimite);

    /**
     * Busca lotes por produto, ordenados pela data de validade.
     * @param produtoId ID do produto.
     * @return Lista de lotes para o produto, ordenados por data de validade (mais antigos primeiro).
     */
    List<LoteValidade> findByProdutoIdOrderByDataValidadeAsc(Long produtoId);
}