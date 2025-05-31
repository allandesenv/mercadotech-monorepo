package com.mercadotech.stockservice.enums;

public enum TipoSaida {
    VENDA,      // Saída devido a uma venda de produto
    PERDA,      // Saída devido a produtos perdidos (danificados, extraviados, vencidos)
    CONSUMO,    // Saída para consumo interno (ex: uso de insumos na cozinha)
    AJUSTE,     // Saída para ajustes de inventário
    DEVOLUCAO   // Saída por devolução ao fornecedor (se aplicável)
}