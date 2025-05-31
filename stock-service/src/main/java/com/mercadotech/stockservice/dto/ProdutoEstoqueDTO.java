package com.mercadotech.stockservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data // Gera getters, setters, toString, equals e hashCode do Lombok
@NoArgsConstructor // Gera construtor sem argumentos do Lombok
@AllArgsConstructor // Gera construtor com todos os argumentos do Lombok
@Builder // Gera um builder para a classe
public class ProdutoEstoqueDTO {
    private Long id;
    private String name; // RENOMEADO para corresponder a Product.name
    private BigDecimal price; // RENOMEADO para corresponder a Product.price

    // Para a categoria, se o product-service retornar a entidade Product completa (com Category aninhada),
    // você precisará de uma maneira de extrair o nome da categoria no seu serviço.
    // Uma abordagem comum é ter um ProductResponseDTO no product-service que já retorne o categoryName diretamente.
    // No entanto, se você está recebendo o objeto Category completo, podemos ajustar aqui.
    // Por simplicidade e clareza, vamos assumir que você receberá o nome da categoria diretamente aqui,
    // ou que você fará a extração após receber a resposta do Feign.
    // Se o product-service retornar Category como um objeto, considere:
    // private CategoryDTO category; // E crie esta classe CategoryDTO no stock-service/dto
    // Para este cenário, vamos manter o nome da categoria como String, assumindo que você irá extraí-lo.
    private String categoryName; // Campo para o nome da categoria (ex: "Alimentos", "Eletrônicos")

    // O campo 'descricao' foi REMOVIDO, pois não existe diretamente na entidade Product do product-service.
    // Se precisar de uma descrição, ela deverá ser adicionada à entidade Product no product-service primeiro.
}