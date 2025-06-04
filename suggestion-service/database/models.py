from sqlalchemy import Column, Integer, String, Float, DateTime, Date
from .database import Base # Importa a Base que definimos em database.py
from datetime import datetime, date # Importa datetime e date

# Modelo para a tabela de histórico de vendas
class SaleData(Base):
    __tablename__ = "sales_data" # Nome da tabela no banco de dados

    id = Column(Integer, primary_key=True, index=True)
    produto_id = Column(Integer, index=True, nullable=False)
    quantidade = Column(Integer, nullable=False)
    valor_unitario = Column(Float, nullable=False)
    data_venda = Column(Date, nullable=False) # Usar Date para guardar apenas a data

# Modelo para a tabela de saldo de estoque
class StockData(Base):
    __tablename__ = "stock_data" # Nome da tabela no banco de dados

    id = Column(Integer, primary_key=True, index=True)
    produto_id = Column(Integer, index=True, nullable=False)
    quantidade_atual = Column(Integer, nullable=False)
    data_registro = Column(Date, nullable=False) # Usar Date para guardar apenas a data