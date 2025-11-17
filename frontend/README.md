# BTI Dashboard - Frontend

Dashboard web desenvolvido em Angular para visualização de métricas do Bot de Ajuda BTI.

## 🏗️ Estrutura do Projeto

```
frontend/
├── src/
│   ├── app/
│   │   ├── api/
│   │   │   └── api.service.ts          # Serviço de comunicação com API
│   │   ├── components/
│   │   │   └── dashboard/
│   │   │       ├── dashboard.ts        # Componente principal
│   │   │       ├── dashboard.html      # Template
│   │   │       ├── dashboard.css       # Estilos
│   │   │       └── dashboard.spec.ts   # Testes
│   │   ├── app.config.ts               # Configuração da aplicação
│   │   ├── app.routes.ts               # Rotas
│   │   └── app.ts                      # Componente raiz
│   ├── environments/
│   │   ├── environment.ts              # Ambiente de desenvolvimento
│   │   └── environment.prod.ts         # Ambiente de produção
│   ├── index.html
│   ├── main.ts
│   └── styles.css                      # Estilos globais (Tailwind)
├── scripts/
│   └── set-env.js                      # Script para injetar env vars
├── angular.json
├── package.json
├── tailwind.config.js
├── tsconfig.json
└── vercel.json                         # Configuração Vercel
```

## 🚀 Configuração

### 1. Instalar Dependências

```bash
npm install
```

### 2. Configurar Variáveis de Ambiente

#### Desenvolvimento

Edite `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/logs',
};
```

#### Produção (Vercel)

Configure no dashboard da Vercel:

- **Nome:** `NG_APP_API_URL`
- **Valor:** `https://bti-api-532272487553.northamerica-south1.run.app/api/logs`

O script `scripts/set-env.js` injeta automaticamente no build.

### 3. Executar Localmente

```bash
npm start
```

Acesse: `http://localhost:4200`

## 🎨 Componentes

### DashboardComponent

Componente principal que exibe:

1. **Cards de Métricas**

   - Total de comandos executados
   - Número de usuários únicos

2. **Gráfico de Barras**

   - Visualização dos comandos mais utilizados
   - Implementado com Chart.js

3. **Skeleton Screens**
   - Melhora UX durante carregamento
   - Animação suave com Tailwind CSS

### ApiService

Serviço responsável pela comunicação com o backend:

```typescript
export interface ApiData {
  counts: { [key: string]: number };
  totalReceived: number;
  differentUsers: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  getData(): Observable<ApiData> {
    return this.http.get<ApiData>(`${environment.apiUrl}/stats`);
  }
}
```

## 🎨 Estilização

### Tailwind CSS

O projeto usa Tailwind CSS para estilização:

```css
/* src/styles.css */
@tailwind base;
@tailwind components;
@tailwind utilities;
```

### Configuração

```javascript
// tailwind.config.js
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {},
  },
  plugins: [],
};
```

## 📦 Build

### Desenvolvimento

```bash
npm run build
```

### Produção

```bash
npm run build:prod
```

O script `build:prod` executa:

1. `node scripts/set-env.js` → Injeta variáveis de ambiente
2. `npx ng build --configuration=production` → Build otimizado

Saída: `dist/frontend/browser/`

## 🌐 Deploy (Vercel)

### Automático via GitHub

1. Conecte o repositório no dashboard da Vercel
2. Configure variáveis de ambiente
3. Cada push na branch `main` dispara deploy automático

### Manual via CLI

```bash
npm install -g vercel
vercel --prod
```

### Configuração (`vercel.json`)

```json
{
  "buildCommand": "npm ci && npm run build:prod",
  "outputDirectory": "dist/frontend/browser",
  "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
}
```

## 🧪 Testes

### Unitários

```bash
npm test
```

### E2E

```bash
npm run e2e
```

## 📊 Bibliotecas Principais

- **Angular 20.1**: Framework
- **Chart.js + ng2-charts**: Gráficos
- **Tailwind CSS 3.4**: Estilização
- **RxJS 7.8**: Programação reativa
- **TypeScript 5.8**: Linguagem

## 🔧 Scripts Úteis

| Script               | Descrição                          |
| -------------------- | ---------------------------------- |
| `npm start`          | Inicia servidor de desenvolvimento |
| `npm run build`      | Build de desenvolvimento           |
| `npm run build:prod` | Build de produção com env vars     |
| `npm test`           | Executa testes unitários           |
| `npm run watch`      | Build contínuo em modo watch       |

## 🎯 Features

- ✅ **Standalone Components**: Sem NgModules
- ✅ **Signals**: Gerenciamento de estado reativo
- ✅ **Lazy Loading**: Carregamento sob demanda
- ✅ **Responsive Design**: Mobile-first
- ✅ **Skeleton Screens**: Feedback visual durante carregamento
- ✅ **Environment Variables**: Configuração por ambiente
- ✅ **Production Build**: Otimizado e minificado

## 🔍 Debugging

No Chrome DevTools:

1. Abra o console (F12)
2. Vá em **Sources** > `webpack://` > `src/app`
3. Coloque breakpoints no TypeScript original

## 📝 Convenções de Código

- **Components**: PascalCase (ex: `DashboardComponent`)
- **Services**: PascalCase + `Service` (ex: `ApiService`)
- **Files**: kebab-case (ex: `dashboard.component.ts`)
- **Prettier**: Formatação automática configurada

## 🤝 Contribuindo

1. Siga o [Angular Style Guide](https://angular.dev/style-guide)
2. Use Prettier para formatação
3. Escreva testes para novos componentes
4. Mantenha os READMEs atualizados# Frontend

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 20.1.6.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
