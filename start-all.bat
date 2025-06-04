@echo off
REM Este script inicializa todos os microsserviços do projeto MercadoTech
REM na ordem correta, abrindo um novo prompt de comando para cada um.
REM Execute este script na raiz do seu monorepo (mercadotech-monorepo\).

echo ===================================================
echo  Iniciar Todos os Microsserviços MercadoTech
echo ===================================================
echo Certifique-se de que o PostgreSQL esta rodando e os bancos criados.
echo Certifique-se de ter executado "mvn clean install" na raiz do monorepo.
echo ===================================================
echo.

REM Ordem de inicialização:

REM 1. Config Server
echo Iniciando Config Server na porta 8888...
start cmd.exe /k "cd config-server && mvn spring-boot:run"
timeout /t 10 /nobreak
echo Config Server iniciado.

REM 2. Discovery Service (Eureka)
echo Iniciando Discovery Service na porta 8761...
start cmd.exe /k "cd discovery-service && mvn spring-boot:run"
timeout /t 10 /nobreak
echo Discovery Service iniciado.

REM 3. Gateway Service
echo Iniciando Gateway Service na porta 8081...
start cmd.exe /k "cd gateway-service && mvn spring-boot:run"
timeout /t 10 /nobreak
echo Gateway Service iniciado.

REM 4. Auth Service
echo Iniciando Auth Service na porta 8080...
start cmd.exe /k "cd auth-service && mvn spring-boot:run"
timeout /t 10 /nobreak
echo Auth Service iniciado.

REM 5. Product Service
echo Iniciando Product Service na porta 8082...
start cmd.exe /k "cd product-service && mvn spring-boot:run"
timeout /t 10 /nobreak
echo Product Service iniciado.

REM 6. Stock Service
echo Iniciando Stock Service na porta 8084...
start cmd.exe /k "cd stock-service && mvn spring-boot:run"
timeout /t 10 /nobreak
echo Stock Service iniciado.

REM 7. Sales Service
echo Iniciando Sales Service na porta 8085...
start cmd.exe /k "cd sales-service && mvn spring-boot:run"
timeout /t 10 /nobreak
echo Sales Service iniciado.

echo.
echo Todos os servicos foram acionados. Verifique os logs em cada terminal.
echo Pode levar mais alguns segundos para que todos se registrem no Eureka.
echo Acesse o Eureka Dashboard em http://localhost:8761
echo Para parar, use CTRL+C em cada terminal ou "taskkill /F /IM java.exe"