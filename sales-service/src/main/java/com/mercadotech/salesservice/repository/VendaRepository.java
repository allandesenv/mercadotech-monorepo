package com.mercadotech.salesservice.repository;

import com.mercadotech.salesservice.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository // Marca a interface como um componente de repositório Spring
public interface VendaRepository extends JpaRepository<Venda, Long> {
    // Spring Data JPA fornece automaticamente métodos CRUD (save, findById, findAll, delete, etc.)

    /**
     * Busca vendas por um produto específico.
     * @param produtoId ID do produto.
     * @return Lista de vendas para o produto.
     */
    List<Venda> findByProdutoId(Long produtoId);

    /**
     * Busca vendas em um determinado período de datas.
     * @param dataInicio Data de início do período.
     * @param dataFim Data de fim do período.
     * @return Lista de vendas dentro do período.
     */
    List<Venda> findByDataVendaBetween(LocalDateTime dataInicio, LocalDateTime dataFim);
}