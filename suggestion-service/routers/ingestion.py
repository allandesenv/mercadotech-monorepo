from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from datetime import date, datetime # Importar datetime e date

from database import models
from database.database import get_db

# Pydantic models para validação de dados de entrada na API (DTOs)
from pydantic import BaseModel, Field

# Modelos Pydantic para a ingestão de dados
class SaleDataIngest(BaseModel):
    produto_id: int = Field(..., example=101)
    quantidade: int = Field(..., example=5)
    valor_unitario: float = Field(..., example=12.50)
    data_venda: date = Field(..., example="2025-05-29") # Formato YYYY-MM-DD

    class Config:
        json_schema_extra = {
            "example": {
                "produto_id": 101,
                "quantidade": 5,
                "valor_unitario": 12.50,
                "data_venda": "2025-05-29"
            }
        }

class StockDataIngest(BaseModel):
    produto_id: int = Field(..., example=101)
    quantidade_atual: int = Field(..., example=100)
    data_registro: date = Field(..., example="2025-05-29") # Formato YYYY-MM-DD

    class Config:
        json_schema_extra = {
            "example": {
                "produto_id": 101,
                "quantidade_atual": 100,
                "data_registro": "2025-05-29"
            }
        }

router = APIRouter(
    prefix="/dados",
    tags=["Ingestão de Dados"]
)

@router.post("/vendas", status_code=status.HTTP_201_CREATED)
async def ingest_sale_data(sale_data: SaleDataIngest, db: Session = Depends(get_db)):
    """
    Recebe e ingere dados de histórico de vendas.
    """
    # Converte o Pydantic model para SQLAlchemy model
    db_sale_data = models.SaleData(
        produto_id=sale_data.produto_id,
        quantidade=sale_data.quantidade,
        valor_unitario=sale_data.valor_unitario,
        data_venda=sale_data.data_venda
    )
    db.add(db_sale_data) # Adiciona o objeto à sessão
    db.commit() # Salva no banco de dados
    db.refresh(db_sale_data) # Atualiza o objeto com o ID gerado pelo banco
    return {"message": "Dados de venda ingeridos com sucesso!", "id": db_sale_data.id}

@router.post("/estoque", status_code=status.HTTP_201_CREATED)
async def ingest_stock_data(stock_data: StockDataIngest, db: Session = Depends(get_db)):
    """
    Recebe e ingere dados de saldo de estoque.
    """
    # Converte o Pydantic model para SQLAlchemy model
    db_stock_data = models.StockData(
        produto_id=stock_data.produto_id,
        quantidade_atual=stock_data.quantidade_atual,
        data_registro=stock_data.data_registro
    )
    db.add(db_stock_data) # Adiciona o objeto à sessão
    db.commit() # Salva no banco de dados
    db.refresh(db_stock_data) # Atualiza o objeto com o ID gerado pelo banco
    return {"message": "Dados de estoque ingeridos com sucesso!", "id": db_stock_data.id}