# BTI API - Backend

API REST desenvolvida em Spring Boot para gerenciamento de métricas do Bot de Ajuda BTI.

## 🏗️ Estrutura do Projeto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/br/com/btihelpbot/bti_api/
│   │   │   ├── config/
│   │   │   │   └── CorsConfig.java
│   │   │   ├── controller/
│   │   │   │   └── MetricsController.java
│   │   │   ├── dto/
│   │   │   │   ├── CommandLogDTO.java
│   │   │   │   └── StatsSummaryDTO.java
│   │   │   ├── model/
│   │   │   │   └── CommandLog.java
│   │   │   ├── repository/
│   │   │   │   ├── CommandLogRepository.java
│   │   │   │   └── specifications/
│   │   │   │       └── CommandLogSpecifications.java
│   │   │   └── service/
│   │   │       └── MetricsService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── Dockerfile
```

## 🚀 Configuração

### 1. Banco de Dados

Crie um banco PostgreSQL:

```sql
CREATE DATABASE bti_db;
```

### 2. Variáveis de Ambiente e `application.properties`

O projeto utiliza variáveis de ambiente para evitar segredos no código-fonte.

```properties
spring.application.name=bti-api

# Database (definido via env)
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# API Key usada para proteger rotas sensíveis
api.security.key=${API_SECURITY_KEY}
```

Exemplos locais:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/bti_db"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_password"
export API_SECURITY_KEY="your_api_key_here"
```

### 3. Executar

```bash
mvn clean install
mvn spring-boot:run
```

## 📊 Endpoints

As rotas abaixo exigem, quando indicado, o header `X-API-Key` com a chave configurada em `API_SECURITY_KEY`.

### POST `/api/logs/command` (requer `X-API-Key`)

Registra uma execução de comando.

**Request Body:**

```json
{
  "command": "help",
  "userId": "123456",
  "groupId": "789012"
}
```

**Response:** `201 Created`

Exemplo cURL:

```bash
curl -X POST "http://localhost:8080/api/logs/command" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_SECURITY_KEY" \
  -d '{
        "command": "help",
        "userId": "123456",
        "groupId": "789012"
      }'
```

---

### GET `/api/logs` (requer `X-API-Key`)

Lista logs com paginação e filtros.

**Query Parameters:**

- `startDate` (opcional): Data inicial (ISO-8601)
- `endDate` (opcional): Data final (ISO-8601)
- `chatType` (opcional): `GROUP` ou `USER`
- `commands` (opcional): Lista de comandos separados por vírgula
- `page` (opcional): Número da página (default: 0)
- `size` (opcional): Tamanho da página (default: 20)
- `sort` (opcional): Campo e direção, ex: `executedAt,desc`

**Response:**

````json
{
  "content": [

Exemplo cURL com filtros + paginação:

```bash
curl -G "http://localhost:8080/api/logs" \
  -H "X-API-Key: $API_SECURITY_KEY" \
  --data-urlencode "startDate=2025-11-01T00:00:00Z" \
  --data-urlencode "endDate=2025-11-30T23:59:59Z" \
  --data-urlencode "chatType=GROUP" \
  --data-urlencode "commands=help,info" \
  --data-urlencode "page=0" \
  --data-urlencode "size=20" \
  --data-urlencode "sort=executedAt,desc"
````

    {
      "id": 1,
      "command": "help",
      "userId": "123456@c.us",
      "groupId": "789012@g.us",

Retorna resumo estatístico com contagem por comando, total de registros e número de usuários distintos.
}
**Response (StatsSummaryDTO):**
"pageable": {...},
"totalElements": 100,
"totalPages": 5
"counts": {
"help": 450,
"info": 320,
"start": 730
},
"totalReceived": 1500,
"differentUsers": 250

---

### GET `/api/logs/stats`

Retorna contagem agrupada por comando.

**Response:**

```json
{
  "counts": {
    "help": 450,
    "info": 320,
    "start": 730
  },
  "total_received": 1500,
  "different_users": 250
}
```

## 🐳 Docker

### Build

```bash
docker build -t bti-api .
```

### Run (com variáveis de ambiente)

```bash
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/bti_db" \
  -e DB_USERNAME="postgres" \
  -e DB_PASSWORD="your_password" \
  -e API_SECURITY_KEY="your_api_key_here" \
  bti-api
```

## 🧪 Testes

```bash
mvn test
```

## 📦 Dependências Principais

- **Spring Boot Starter Web**: REST API
- **Spring Boot Starter Data JPA**: Persistência
- **PostgreSQL Driver**: Conexão com banco
- **Lombok**: Redução de boilerplate
- **Spring Boot DevTools**: Hot reload em desenvolvimento

## 🔒 CORS

O CORS está configurado em `CorsConfig.java` para aceitar requisições do frontend:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
        .allowedOrigins(
          "http://localhost:4200",
          "https://bti-hp-dashboard.vercel.app" // sem barra final
        )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
```

## 🔐 Segurança (X-API-Key)

As seguintes rotas exigem o header `X-API-Key`:

- `POST /api/logs/command`
- `GET /api/logs`

Quando ausente ou inválida, a API retorna:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Chave de API inválida ou ausente"
}
```

## ⏱️ Datas e Formato

- O campo `executedAt` é um `Instant` (UTC) com timezone.
- Filtros `startDate` e `endDate` aceitam ISO‑8601 (ex.: `2025-11-01T00:00:00Z`).

## 🧭 Dicas / Próximos Passos

- Adicionar OpenAPI/Swagger (`springdoc-openapi-starter-webmvc-ui`) para expor documentação em `/swagger-ui.html`.
- Observação técnica: o filtro por comandos usa Specifications; assegure-se de que o campo filtrado é `command` (mesmo nome presente na entidade `CommandLog`).

## 📝 Model: CommandLog

| Campo        | Tipo    | Descrição                          |
| ------------ | ------- | ---------------------------------- |
| `id`         | Long    | ID auto-incrementado               |
| `command`    | String  | Nome do comando executado          |
| `userId`     | String  | ID do usuário que executou         |
| `groupId`    | String  | ID do grupo (null se chat privado) |
| `executedAt` | Instant | Timestamp da execução              |

## 🔍 Specifications

O projeto usa **Spring Data JPA Specifications** para filtros dinâmicos:

- `byStartDate(ZonedDateTime)`: Filtra por data inicial
- `byEndDate(ZonedDateTime)`: Filtra por data final
- `byChatType(String)`: Filtra por tipo de chat (GROUP/USER)
- `byCommands(List<String>)`: Filtra por lista de comandos

Exemplo:

```java
Specification<CommandLog> spec = Specification
    .where(byStartDate(startDate))
    .and(byEndDate(endDate))
    .and(byChatType("GROUP"));
```
