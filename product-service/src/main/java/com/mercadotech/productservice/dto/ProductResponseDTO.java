// product-service/src/main/java/com/mercadotech/productservice/dto/ProductResponseDTO.java
package com.mercadotech.productservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String categoryName; // Apenas o nome da categoria
    private String unitAbbreviation; // Apenas a abreviação da unidade
}