package com.mercadotech.validityservice.enums;

public enum LoteStatus {
    ATIVO,      // Lote com validade ainda ativa
    VENCIDO,    // Lote cuja data de validade já passou
    BAIXADO,    // Lote que foi dado baixa do estoque (por vencimento ou perda manual)
    PENDENTE_BAIXA // Opcional: Lote que foi marcado para baixa mas ainda não foi processado no estoque
}