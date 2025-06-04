from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# URL do banco de dados SQLite
# O arquivo do banco de dados será 'mercadotech_ia.db' na raiz do projeto.
DATABASE_URL = "sqlite:///./mercadotech_ia.db"

# Cria a engine do SQLAlchemy
# connect_args={"check_same_thread": False} é necessário para SQLite em FastAPI
# porque o SQLite permite apenas um thread por vez para interagir com a mesma conexão.
# FastAPI pode usar múltiplos threads, então essa flag desabilita essa checagem.
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})

# Cria uma SessionLocal
# Cada instância da SessionLocal será uma sessão de banco de dados.
# Essa é a classe que usaremos para criar sessões de banco de dados em nossos endpoints.
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base para os modelos declarativos do SQLAlchemy
Base = declarative_base()

# Função de utilidade para obter uma sessão de banco de dados
# Será usada como uma dependência no FastAPI para gerenciar a sessão de BD por requisição
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()