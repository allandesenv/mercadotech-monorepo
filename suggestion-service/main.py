from fastapi import FastAPI
from database.database import engine, Base # Importa a engine e a Base do seu arquivo database.py
from routers import ingestion, suggestion # Importa os routers (que criaremos em breve)

# Cria as tabelas no banco de dados se elas não existirem
# Isso deve ser executado apenas uma vez, ou sempre que você quiser garantir o schema.
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="MercadoTech Suggestion Service",
    description="Serviço de IA para sugestão de compras e análise de validade.",
    version="1.0.0"
)

# Inclui os routers
app.include_router(ingestion.router) # Router para ingestão de dados
app.include_router(suggestion.router) # Router para sugestão

@app.get("/")
async def root():
    return {"message": "Suggestion Service está online!"}


if __name__ == "__main__":
    import uvicorn
    # Execute com `python main.py` ou `uvicorn main:app --reload --port 8000`
    uvicorn.run(app, host="0.0.0.0", port=8000)