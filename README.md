# BTI Help Bot - Dashboard & API

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Angular](https://img.shields.io/badge/Angular-20.1-red.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)

Sistema de monitoramento e análise de métricas do Bot de Ajuda do BTI, composto por uma API REST em Spring Boot e um dashboard web em Angular.

## 📋 Visão Geral

Este projeto é um monorepo que contém:

- **Backend**: API REST desenvolvida em Spring Boot para coletar e fornecer métricas de uso do bot
- **Frontend**: Dashboard web em Angular para visualização de estatísticas e análise de dados

## 🏗️ Arquitetura

```
bti-api/
├── backend/          # API Spring Boot
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/         # Dashboard Angular
│   ├── src/
│   ├── package.json
│   └── vercel.json
└── README.md
```

## 🚀 Funcionalidades

### Backend (API)

- ✅ Registro de comandos executados pelo bot
- ✅ Endpoints para consulta de métricas e estatísticas
- ✅ Filtragem por data, tipo de chat (grupo/usuário) e comando
- ✅ Contagem de usuários únicos e comandos mais utilizados
- ✅ Suporte a PostgreSQL
- ✅ CORS configurado para integração com frontend
- ✅ Deploy automatizado no Google Cloud Run

### Frontend (Dashboard)

- ✅ Visualização de métricas em tempo real
- ✅ Gráfico de barras com os comandos mais utilizados (Chart.js)
- ✅ Cards com total de comandos e usuários únicos
- ✅ Skeleton screens para melhor UX durante carregamento
- ✅ Design responsivo com Tailwind CSS
- ✅ Deploy automatizado na Vercel

## 🛠️ Tecnologias

### Backend

- **Java 21+**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Maven**

### Frontend

- **Angular 20.1**
- **TypeScript 5.8**
- **Chart.js + ng2-charts**
- **Tailwind CSS 3.4**
- **RxJS**

## 📦 Instalação e Configuração

### Pré-requisitos

- Java 21+
- Node.js 18+
- PostgreSQL 17+
- Maven 4.0+

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`

Veja mais detalhes em [backend/README.md](backend/README.md)

### Frontend

```bash
cd frontend
npm install
npm start
```

O dashboard estará disponível em `http://localhost:4200`

Veja mais detalhes em [frontend/README.md](frontend/README.md)

## 🌐 Deploy

### Backend

Deployado no **Google Cloud Run**:

- URL: `https://bti-api-532272487553.northamerica-south1.run.app`
- Container: Docker
- Auto-scaling habilitado

### Frontend

Deployado na **Vercel**:

- Deploy automático via GitHub
- Variáveis de ambiente configuradas no dashboard da Vercel

## 🔐 Variáveis de Ambiente

### Backend (`application.properties`)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bti_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Frontend (Vercel)

```
NG_APP_API_URL=https://bti-api-532272487553.northamerica-south1.run.app/api/logs
```

## 📊 Endpoints da API

| Método | Endpoint                  | Descrição                          |
| ------ | ------------------------- | ---------------------------------- |
| `POST` | `/api/logs`               | Registra execução de comando       |
| `GET`  | `/api/logs`               | Lista logs com paginação e filtros |
| `GET`  | `/api/logs/stats`         | Retorna resumo estatístico         |
| `GET`  | `/api/logs/command-usage` | Retorna contagem por comando       |

Documentação completa: [backend/README.md](backend/README.md)

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👥 Autores

- **Cássio** - [GitHub](https://github.com/Jcassio-dev)

## 🔗 Links Úteis

- [Documentação Spring Boot](https://spring.io/projects/spring-boot)
- [Documentação Angular](https://angular.dev)
- [Chart.js](https://www.chartjs.org/)
- [Tailwind CSS](https://tailwindcss.com/)
