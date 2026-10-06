# Projeto - Cidades ESG Inteligentes

API REST em **Java 17 + Spring Boot 3** para registrar ocorrências urbanas classificadas nas dimensões **ESG** (Ambiental, Social e Governança), com ciclo de vida totalmente automatizado: build, testes, containerização e deploy em **staging** e **produção**.

**Integrantes:** _(preencher nomes e RMs)_

## Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/ocorrencias?categoria=AMBIENTAL` | Lista (filtro opcional por categoria) |
| GET | `/api/ocorrencias/{id}` | Busca por id |
| POST | `/api/ocorrencias` | Cria ocorrência |
| PUT | `/api/ocorrencias/{id}` | Atualiza |
| DELETE | `/api/ocorrencias/{id}` | Remove |
| GET | `/api/info` | Mostra o ambiente (`local`, `staging`, `production`) |
| GET | `/actuator/health` | Health check |

Exemplo de corpo (`categoria`: `AMBIENTAL`, `SOCIAL` ou `GOVERNANCA`):

```json
{ "titulo": "Descarte irregular de lixo", "descricao": "Entulho na calçada",
  "categoria": "AMBIENTAL", "bairro": "Centro" }
```

## Como executar localmente com Docker

Pré-requisito: Docker com Docker Compose.

```bash
# 1. Configurar variáveis de ambiente
cp .env.example .env          # edite POSTGRES_PASSWORD

# 2. Construir a imagem e subir aplicação + PostgreSQL
docker compose up -d --build

# 3. Verificar
docker compose ps
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/info

# 4. Criar e listar ocorrências
curl -X POST http://localhost:8080/api/ocorrencias -H "Content-Type: application/json" \
  -d '{"titulo":"Descarte irregular","categoria":"AMBIENTAL","bairro":"Centro"}'
curl http://localhost:8080/api/ocorrencias

# 5. Encerrar (use -v para apagar também os volumes)
docker compose down
```

Rodar apenas os testes (sem Docker): `mvn verify` (usa banco H2 em memória).

## Pipeline CI/CD

**Ferramenta:** GitHub Actions (`.github/workflows/ci-cd.yml` e `deploy.yml`).

| # | Etapa (job) | O que faz | Quando roda |
|---|-------------|-----------|-------------|
| 1 | `build-test` | `mvn -B verify`: compila e executa os testes automatizados; publica o relatório do Surefire | Push e Pull Request |
| 2 | `docker` | Builda a imagem; em push na `main`, publica no GitHub Container Registry (`ghcr.io`) com tags `<sha>` e `latest` | Push e PR (push da imagem só na `main`) |
| 3 | `deploy-staging` | Copia o compose via SSH, gera o `.env`, faz `pull` da imagem e `up -d` na porta **8081**; valida com health check | Push na `main` |
| 4 | `deploy-production` | Mesmo fluxo na porta **8080**, somente após staging OK | Push na `main` |

Cada etapa só executa se a anterior passar. O deploy usa um workflow reutilizável (`deploy.yml`) chamado duas vezes, com `environment` diferente. As proteções de cada *Environment* do GitHub permitem exigir **aprovação manual** antes de produção.

### Configuração necessária no GitHub

1. *Settings → Environments*: criar `staging` e `production` (em `production`, ativar *Required reviewers*).
2. Em **cada** environment, cadastrar os secrets:
   - `SSH_HOST` – IP/host do servidor com Docker + Docker Compose + curl
   - `SSH_USER` – usuário SSH (com permissão de usar Docker)
   - `SSH_KEY` – chave privada SSH
   - `DB_PASSWORD` – senha do PostgreSQL daquele ambiente (use apenas letras e números)
3. *Settings → Actions → General → Workflow permissions*: **Read and write**.
4. Staging e produção podem ser o mesmo servidor: os ambientes ficam isolados por projeto Compose (`esg-staging`, `esg-production`), pastas, volumes e portas distintas.

## Containerização

**Estratégias adotadas:** build multi-stage (JDK só no build, JRE no runtime), imagem final Alpine, execução com usuário não-root, cache de dependências Maven em camada própria, `HEALTHCHECK` e configuração 100% por variáveis de ambiente.

```dockerfile
# ---------- Estagio 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Estagio 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S esg && adduser -S esg -G esg
WORKDIR /app
RUN mkdir -p /app/logs && chown -R esg:esg /app
COPY --from=build --chown=esg:esg /build/target/*.jar app.jar

USER esg
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
```

**Orquestração (`docker-compose.yml`):**

- **Serviços:** `app` (Spring Boot) e `db` (PostgreSQL 16).
- **Rede:** `esg-net` (bridge); o banco não expõe porta ao host.
- **Volumes:** `pgdata` (dados do banco) e `app-logs` (logs da aplicação).
- **Variáveis de ambiente:** vindas do `.env` (modelo em `.env.example`).
- **Dependência:** `app` só inicia quando o healthcheck do `db` está saudável.

## Prints do funcionamento

> Salve as imagens em `docs/evidencias/` e referencie abaixo.

| Evidência | Arquivo |
|-----------|---------|
| Pipeline executado com sucesso (build, testes, imagem, deploys) | `![pipeline](docs/evidencias/pipeline.png)` |
| Relatório de testes | `![testes](docs/evidencias/testes.png)` |
| Staging – `/api/info` com `"ambiente":"staging"` | `![staging](docs/evidencias/staging.png)` |
| Produção – `/api/info` com `"ambiente":"production"` | `![producao](docs/evidencias/producao.png)` |
| `docker compose ps` local | `![compose](docs/evidencias/compose-ps.png)` |

- URL staging: `http://<SSH_HOST>:8081/api/info`
- URL produção: `http://<SSH_HOST>:8080/api/info`
- Execução do pipeline: _(colar link da aba Actions)_

## Tecnologias utilizadas

Java 17 · Spring Boot 3.3 (Web, Data JPA, Validation, Actuator) · Maven · PostgreSQL 16 · H2 (testes) · JUnit 5 / MockMvc · Docker · Docker Compose · GitHub Actions · GitHub Container Registry · SSH.

## Estrutura do projeto

```
cidades-esg-inteligentes/
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── pom.xml
├── README.md
├── src/                      # código-fonte e testes
├── docs/                     # documentação em PDF e evidências
└── .github/workflows/        # ci-cd.yml e deploy.yml
```

## Checklist de entrega

| Item | OK |
|------|----|
| Projeto compactado em .ZIP com estrutura organizada | ☑ |
| Dockerfile funcional | ☑ |
| docker-compose.yml ou arquivos Kubernetes | ☑ |
| Pipeline com etapas de build, teste e deploy | ☑ |
| README.md com instruções e prints | ☐ _(inserir prints)_ |
| Documentação técnica com evidências (PDF ou PPT) | ☐ _(inserir prints no PDF)_ |
| Deploy realizado nos ambientes staging e produção | ☐ _(após configurar secrets e rodar o pipeline)_ |
