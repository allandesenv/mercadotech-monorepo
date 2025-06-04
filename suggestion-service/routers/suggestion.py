from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import func # Para funções de agregação como SUM
import pandas as pd
from datetime import date, timedelta # Importar timedelta

from database import models
from database.database import get_db

# Pydantic models para validação de dados de saída da API (DTOs de resposta)
from pydantic import BaseModel, Field

# Modelos Pydantic para a resposta do endpoint de sugestão
class SuggestionResponse(BaseModel):
    produto_id: int = Field(..., example=101)
    quantidade_sugerida: int = Field(..., example=75)
    metodo_previsao: str = Field(..., example="Média Móvel Semanal (4 semanas)")
    observacao: str = Field(None, example="Baseado em dados históricos de vendas. Sugestão mínima.")

    class Config:
        json_schema_extra = {
            "example": {
                "produto_id": 101,
                "quantidade_sugerida": 75,
                "metodo_previsao": "Média Móvel Semanal (4 semanas)",
                "observacao": "Baseado em dados históricos de vendas. Sugestão mínima."
            }
        }

router = APIRouter(
    prefix="/sugestao",
    tags=["Sugestão de Compra"]
)

@router.get("/{produto_id}", response_model=SuggestionResponse)
async def get_sugestao_compra(produto_id: int, db: Session = Depends(get_db)):
    """
    Retorna uma sugestão de quantidade de compra para um produto, baseada em IA.
    """
    # 1. Obter dados de vendas históricas para o produto
    sales_records = db.query(models.SaleData).filter(models.SaleData.produto_id == produto_id).all()

    if not sales_records:
        # Fallback para produtos sem histórico de vendas
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Dados de venda não encontrados para o produto ID {produto_id}. Sem histórico para sugestão."
        )

    # Converter para DataFrame do Pandas para facilitar a manipulação
    df_sales = pd.DataFrame([s.__dict__ for s in sales_records])
    df_sales['data_venda'] = pd.to_datetime(df_sales['data_venda'])
    df_sales.set_index('data_venda', inplace=True)

    # Agrupar vendas por semana para cálculo da média móvel
    weekly_sales = df_sales.resample('W')['quantidade'].sum().sort_index()

    # Parâmetros da média móvel
    window_size_weeks = 4 # Janela de 4 semanas

    if len(weekly_sales) < window_size_weeks:
        # Fallback para produtos com poucos dados (menos de 4 semanas de vendas)
        # Neste caso, podemos sugerir a quantidade da última venda ou uma média simples.
        last_sale_qty = sales_records[-1].quantidade if sales_records else 0
        avg_qty = df_sales['quantidade'].mean() if not df_sales.empty else 0
        
        # Obter o último saldo de estoque para uma sugestão mais inteligente no fallback
        last_stock_record = db.query(models.StockData).filter(models.StockData.produto_id == produto_id) \
                                .order_by(models.StockData.data_registro.desc()).first()
        current_stock = last_stock_record.quantidade_atual if last_stock_record else 0

        # Heurística simples para fallback: se há poucas vendas, sugere a última venda ou uma média se for maior que o estoque atual
        fallback_qty = max(last_sale_qty, int(avg_qty * 1.5)) # Sugere um pouco mais que a média, ou a última venda
        
        # Considera o estoque atual para não sugerir compra se já tiver muito
        # Sugere comprar apenas se o estoque atual for menor que a média de vendas ou a última venda
        if current_stock >= fallback_qty:
            suggested_quantity = 0 # Não sugerir compra se já tiver estoque suficiente para o período de fallback
            observation = f"Dados insuficientes ({len(weekly_sales)} semanas). Estoque atual ({current_stock} unidades) já cobre a demanda de fallback."
        else:
            suggested_quantity = max(0, fallback_qty - current_stock) # Sugere a diferença
            observation = f"Dados insuficientes ({len(weekly_sales)} semanas). Sugestão baseada em última venda/média simples ({fallback_qty} unidades) menos estoque atual ({current_stock} unidades)."

        return SuggestionResponse(
            produto_id=produto_id,
            quantidade_sugerida=suggested_quantity,
            metodo_previsao="Fallback: Dados Insuficientes",
            observacao=observation
        )

    # Calcular a média móvel semanal
    # A média móvel é calculada sobre as `window_size_weeks` semanas mais recentes.
    moving_average_weekly = weekly_sales.rolling(window=window_size_weeks).mean().iloc[-1]
    
    # Arredondar para o inteiro mais próximo e garantir que não seja negativo
    suggested_quantity = max(0, int(round(moving_average_weekly)))

    # Você pode querer adicionar uma margem de segurança ou considerar estoque atual aqui.
    # Exemplo simples: Sugerir a média móvel como quantidade a ser comprada.
    # Para um modelo mais complexo, você subtrairia o estoque atual, adicionaria lead time, etc.

    return SuggestionResponse(
        produto_id=produto_id,
        quantidade_sugerida=suggested_quantity,
        metodo_previsao=f"Média Móvel Semanal ({window_size_weeks} semanas)",
        observacao="Sugestão baseada no consumo histórico semanal."
    )