package com.mercadotech.stockservice.repository;

import com.mercadotech.stockservice.entity.SaidaEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Marca a interface como um componente de repositório Spring
public interface SaidaEstoqueRepository extends JpaRepository<SaidaEstoque, Long> {
    // Spring Data JPA fornece automaticamente métodos CRUD (save, findById, findAll, delete, etc.)

    // Exemplo de método de consulta personalizado
    List<SaidaEstoque> findByProdutoIdOrderByDataSaidaAsc(Long produtoId);
}