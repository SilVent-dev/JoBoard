# Joboard — Backend

API REST do **Joboard**, um ATS pessoal (*Applicant Tracking System*) para quem está em busca ativa de emprego: um lugar único para catalogar empresas, rastrear vagas, acompanhar candidaturas etapa por etapa, guardar versões de currículo e registrar os contatos de cada processo seletivo.

Não é uma plataforma de vagas — é uma ferramenta **privada e individual**. Cada usuário vê apenas os próprios dados, garantido por design em todas as camadas.

> Frontend (Vue 3): [SilVent-dev/JoBoard-Front](https://github.com/SilVent-dev/JoBoard-Front)

---

## Funcionalidades

- **Autenticação** — cadastro público com verificação de email obrigatória (token de uso único, 2h), login com JWT (2h), redefinição de senha ("esqueci minha senha" com token de 30 min e uso único)
- **Perfil profissional** — dados de carreira, pretensão salarial, links e preferências de modelo de trabalho
- **Catálogo de empresas** — com anotações de cultura e contatos de referência
- **Vagas rastreadas** — vinculadas a empresas, com filtros por modelo de trabalho, contrato e nível
- **Candidaturas** — pipeline de 10 status com mapa de transições válidas, histórico automático de eventos, avaliações pessoais, notas e recandidatura após arquivamento
- **Próxima ação automática** — ao avançar de etapa, o sistema sugere o follow-up adequado; job diário envia por email as ações do dia (opt-out no perfil)
- **Insights** — funil de conversão, tempo médio em cada etapa e taxa de resposta, derivados do histórico
- **Currículos** — upload de PDF (até 5MB) com versionamento e currículo principal, armazenados no Supabase Storage
- **Contatos** — pessoas conhecidas em cada processo (recrutador, tech lead, RH...)
- **LGPD** — exportação de todos os dados do titular (ZIP de CSVs) e exclusão de conta em cascata, incluindo arquivos no Storage

## Stack

| | |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 4 (Web MVC, Data JPA, Security, Validation, Mail, Scheduling) |
| Banco | PostgreSQL (Supabase) + Flyway (migrações versionadas) |
| Autenticação | JWT (auth0 java-jwt, HMAC256) + BCrypt |
| Rate limiting | Bucket4j (em memória, por IP) |
| Arquivos | Supabase Storage via HTTP |
| Build | Maven (wrapper incluído) |

## Arquitetura

Arquitetura em camadas derivada do MVC, para uma API REST stateless:

```
br.com.joboard/
├── controlador/     → REST Controllers — porta de entrada HTTP
├── dominio/
│   ├── entidade/    → Entidades JPA
│   ├── enums/       → Valores de domínio fechados
│   ├── DTO/         → Entrada e saída da API (um DTO por operação)
│   ├── excecao/     → Exceções de domínio + GlobalExceptionHandler
│   └── evento/      → Domain Events
├── repositorio/     → Interfaces Spring Data JPA
├── servico/         → Regras de negócio
├── seguranca/       → JWT, filtros, rate limit, UserDetails
└── listener/        → Listeners de eventos de domínio
```

**Regra de ouro:** o controller nunca acessa o repository — todo caminho passa pelo service, entrada e saída sempre via DTO (`from()` estático para mapeamento). Regra de negócio mora exclusivamente no service.

### Bounded contexts

```
Identity & Access ──► Document Management ──► Application Tracking ──► Relationship Mgmt
(Usuario, Perfil)     (Curriculo)             (Empresa, Vaga,          (Contatos)
                                               Candidatura, Histórico)
```

### Segurança em três camadas

1. **CORS** — origens permitidas configuráveis por ambiente
2. **JWT** — `SecurityFilter` valida o token e popula o contexto a cada requisição
3. **Ownership na query** — recursos são buscados via `findByIdAndUsuarioId(...)`; recurso inexistente **ou de outro usuário** responde **404**, nunca revelando que o recurso existe

Endurecimento do cadastro público: rate limiting por IP (429) e por email, e honeypot anti-bot no cadastro.

### Semântica de erros

O `GlobalExceptionHandler` é a única classe que conhece HTTP status codes:

| Status | Significado |
|---|---|
| 400 | Requisição malformada (Bean Validation) |
| 401 | Credenciais inválidas |
| 403 | Autenticação ausente/inválida ou conta pendente/bloqueada |
| 404 | Recurso inexistente **ou de outro usuário** |
| 409 | Conflito com estado existente (duplicidade) |
| 422 | Regra de negócio violada (vaga fechada, transição inválida, token expirado...) |
| 429 | Limite de requisições excedido |

## Endpoints principais

| Recurso | Base | Operações |
|---|---|---|
| Autenticação | `/auth` | login, cadastro, verificar-email, esqueci-senha, redefinir-senha |
| Perfil | `/api/perfil` | GET, POST, PUT |
| Currículos | `/api/curriculos` | upload (multipart), listar, marcar principal, deletar |
| Empresas | `/api/empresas` | CRUD |
| Vagas | `/api/vagas` | CRUD + filtros + fechar |
| Candidaturas | `/api/candidaturas` | criar, listar, detalhar, avançar status, nota, arquivar, histórico |
| Contatos | `/api/candidaturas/{id}/contatos` | CRUD aninhado |
| Insights | `/api/insights` | GET |
| Conta (LGPD) | `/api/conta` | DELETE (exclusão total), GET `/exportacao` (ZIP de CSVs) |

Rotas fora de `/auth/*` exigem `Authorization: Bearer <jwt>`.

## Como rodar

### Pré-requisitos

- **JDK 25**
- Banco PostgreSQL acessível (o projeto usa Supabase)
- Servidor SMTP para os emails transacionais

### Configuração

Os segredos **não** ficam no repositório. Crie `src/main/resources/application-local.properties` (já ignorado pelo git) com:

```properties
DB_URL=jdbc:postgresql://<host>:5432/postgres
DB_USERNAME=<usuario>
DB_PASSWORD=<senha>

JWT_SECRET=<segredo aleatório — ex.: openssl rand -hex 32>

MAIL_USERNAME=<usuario smtp>
MAIL_PASSWORD=<senha smtp>

SUPABASE_URL=https://<projeto>.supabase.co
SUPABASE_SERVICE_ROLE_KEY=<service role key>
```

Em produção, as mesmas chaves são fornecidas como variáveis de ambiente — a lista completa está no `DEPLOY.md` (repositório-pai), junto com o guia de Railway/Render, Vercel, provedor de email (SPF/DKIM) e Sentry.

### Subindo a aplicação

```bash
./mvnw spring-boot:run
```

No primeiro start, o Flyway faz o baseline do schema e aplica as migrações de `src/main/resources/db/migration/`.

> **Windows:** se houver mais de um JDK instalado, aponte o `JAVA_HOME` para o JDK 25 antes do `mvnw`.

### Testes

```bash
./mvnw test
```

Testes unitários dos services (JUnit 5 + Mockito), cobrindo as regras críticas: ownership entre usuários (sempre 404), tokens de uso único (verificação e reset), honeypot, exclusão de conta (senha obrigatória e ordem storage → banco), job de follow-up e cálculo de insights.

### Docker

```bash
docker build -t joboard-api .
docker run -p 8080:8080 --env-file .env joboard-api
```

## Decisões de projeto

- **404 em vez de 403 para recurso alheio** — não revelar existência é decisão deliberada de segurança; 403 fica reservado para autenticação
- **Resposta genérica no esqueci-senha** — a mesma mensagem é retornada exista o email ou não (anti-enumeração)
- **Enums sempre `@Enumerated(STRING)`**, transições de status em `EnumMap` estático, `FetchType.LAZY` em todo `@ManyToOne`
- **`@Transactional` só no service**, obrigatório em operações compostas
- **Storage antes do banco** nas deleções que envolvem arquivos — falha no Storage mantém o banco consistente
- **Domain Events** para efeitos colaterais de email (conta pendente, reset de senha) — handler HTTP não conhece o serviço de email
- **Minimização de dados (LGPD)** — não coletamos CPF; conta é excluível e exportável pelo próprio titular

## Licença

Projeto pessoal — todos os direitos reservados.
