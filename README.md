# Joboard — Backend

> Sistema pessoal de rastreamento de candidaturas a vagas de emprego.
 
![Java](https://img.shields.io/badge/Java-25-orange?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4-6DB33F?style=flat&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-4169E1?style=flat&logo=postgresql&logoColor=white)
![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow?style=flat)
 
API REST construída com Java e Spring Boot, com autenticação JWT stateless, arquitetura em camadas e quatro bounded contexts bem definidos. O frontend em Vue.js está em [repositório separado](https://github.com/SilVent-dev/joboard-frontend).

---

## Sobre o projeto

O Joboard resolve um problema real: o caos de acompanhar dezenas de candidaturas simultâneas em plataformas diferentes. O sistema permite que o candidato cadastre empresas, rastreie vagas, acompanhe cada processo seletivo com histórico automático de eventos, registre contatos feitos durante o processo e gerencie múltiplas versões de currículo.
 
Cada usuário acessa apenas os próprios dados — ownership validado em toda operação.

---

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 25 |
| Spring Boot | 4 |
| Spring Security | JWT stateless |
| PostgreSQL | via Supabase |
| Storage | Supabase Storage |
| Build | Maven |

---

## Arquitetura

```
br.com.joboard
├── controlador/     → REST Controllers
├── dominio/
│   ├── entidade/    → Entidades JPA
│   ├── enums/       → Enums de domínio
│   ├── DTO/         → Data Transfer Objects
│   ├── excecao/     → Exceções de domínio
│   └── evento/      → Domain Events
├── repositorio/     → Spring Data JPA
├── servico/         → Regras de negócio
├── seguranca/       → JWT, Filtros, UserDetails
└── listener/        → Listeners de eventos
```

### Bounded Contexts

| Contexto | Responsabilidade | Entidades |
|---|---|---|
| Identity & Access | Autenticação e perfil | Usuario, PerfilCandidato |
| Document Management | Gestão de currículos | Curriculo |
| Application Tracking | Rastreamento de candidaturas | Candidatura, VagaRastreada, EmpresaCatalogada, HistoricoCandidatura |
| Relationship Management | Contatos do processo | Contato |

---

## Autenticação
 
O sistema utiliza JWT stateless. O fluxo completo é:
 
```
1. POST /auth/cadastro  → cria conta e dispara email de verificação
2. GET  /auth/verificar-email?token=  → ativa a conta
3. POST /auth/login  → retorna o JWT
4. Todas as rotas /api/**  → exigem o header: Authorization: Bearer <token>
```
 
O token não é armazenado no servidor — cada requisição é validada de forma independente pela assinatura JWT.
 
---
 
## Endpoints
 
Todas as rotas sob `/api/**` exigem autenticação via `Authorization: Bearer <token>`.
 
### Autenticação — público
 
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/cadastro` | Cria conta e envia email de verificação |
| `POST` | `/auth/login` | Autentica e retorna JWT |
| `GET` | `/auth/verificar-email?token=` | Confirma email e ativa conta |
 
### Perfil
 
| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/perfil` | Retorna perfil do candidato logado |
| `PUT` | `/api/perfil` | Cria ou atualiza perfil |
 
### Currículos
 
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/curriculos` | Upload de PDF |
| `GET` | `/api/curriculos` | Lista currículos |
| `PATCH` | `/api/curriculos/{id}/principal` | Marca como principal |
| `DELETE` | `/api/curriculos/{id}` | Remove currículo e arquivo |
 
### Empresas
 
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/empresas` | Cadastra empresa |
| `GET` | `/api/empresas` | Lista empresas |
| `GET` | `/api/empresas/{id}` | Busca por id |
| `PUT` | `/api/empresas/{id}` | Atualiza empresa |
| `DELETE` | `/api/empresas/{id}` | Remove empresa |
 
### Vagas
 
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/vagas` | Cadastra vaga |
| `GET` | `/api/vagas` | Lista vagas com filtros opcionais |
| `GET` | `/api/vagas/{id}` | Busca por id |
| `PUT` | `/api/vagas/{id}` | Atualiza vaga |
| `PATCH` | `/api/vagas/{id}/fechar` | Marca vaga como fechada |
| `DELETE` | `/api/vagas/{id}` | Remove vaga |
 
Filtros disponíveis: `?modeloTrabalho=REMOTO`, `?tipoContrato=CLT`, `?nivelExperiencia=PLENO`, `?vagaAindaAberta=true`
 
### Candidaturas
 
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/candidaturas` | Cria candidatura |
| `GET` | `/api/candidaturas` | Lista candidaturas ativas |
| `GET` | `/api/candidaturas/{id}` | Busca por id |
| `PATCH` | `/api/candidaturas/{id}/status` | Avança status do processo |
| `PATCH` | `/api/candidaturas/{id}/nota` | Adiciona nota |
| `PATCH` | `/api/candidaturas/{id}/arquivar` | Arquiva candidatura |
| `GET` | `/api/candidaturas/{id}/historico` | Timeline de eventos |
 
### Contatos
 
| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/candidaturas/{id}/contatos` | Adiciona contato |
| `GET` | `/api/candidaturas/{id}/contatos` | Lista contatos |
| `PUT` | `/api/candidaturas/{id}/contatos/{cId}` | Atualiza contato |
| `DELETE` | `/api/candidaturas/{id}/contatos/{cId}` | Remove contato |
 
---

## Fluxo de status da candidatura

```
LISTA_DESEJO → APLICADA → TRIAGEM_TELEFONICA → ENTREVISTA_TECNICA
                                                      ↓
                                        ENTREVISTA_COMPORTAMENTAL
                                                      ↓
                                            TESTE_PRATICO
                                                      ↓
                                          PROPOSTA_RECEBIDA
                                                      ↓
                                               ACEITA ✓

Em qualquer etapa → REJEITADA ou DESISTIDA
```

Transições inválidas são bloqueadas com `422 Unprocessable Entity`. Ao atingir um estado final, o campo `resultadoFinal` é preenchido automaticamente.

---

## Como rodar localmente

### Pré-requisitos

- Java 25
- Maven
- Conta no Supabase (banco PostgreSQL + Storage)
- Conta no serviço de email (Gmail com app password ou similar)

### Configuração
 
Crie o arquivo `src/main/resources/application.properties` com base no exemplo abaixo:
 
```properties
# Banco
spring.datasource.url=jdbc:postgresql://<host>:<port>/<database>
spring.datasource.username=<usuario>
spring.datasource.password=<senha>
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
 
# JWT
api.security.token.secret=${JWT_SECRET:seu-secret-aqui}
 
# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<seu-email>
spring.mail.password=<app-password>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
 
# Supabase Storage
supabase.url=<url-do-projeto>
supabase.key=<service-role-key>
supabase.bucket=curriculos
 
# App
app.url=http://localhost:8080
```
 
### Variáveis de ambiente
 
| Variável | Obrigatória | Descrição |
|---|---|---|
| `spring.datasource.url` | ✅ | URL JDBC do PostgreSQL (Supabase) |
| `spring.datasource.username` | ✅ | Usuário do banco |
| `spring.datasource.password` | ✅ | Senha do banco |
| `JWT_SECRET` | ✅ | Segredo para assinar os tokens JWT |
| `spring.mail.username` | ✅ | Email remetente |
| `spring.mail.password` | ✅ | App Password do serviço de email |
| `supabase.url` | ✅ | URL do projeto Supabase |
| `supabase.key` | ✅ | Service Role Key do Supabase |
| `supabase.bucket` | ✅ | Nome do bucket para armazenar PDFs |

### Rodando

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## Padrões do projeto

| Padrão | Descrição |
|---|---|
| Ownership | Todo recurso é validado por `findByIdAndUsuarioId` — usuário só acessa os próprios dados |
| Exceções de domínio | Exceções específicas por conceito de negócio, nunca `RuntimeException` genérica |
| DTO + Bean Validation | Validação na porta de entrada — nunca na entidade |
| `@Enumerated(EnumType.STRING)` | Todos os enums salvos por nome no banco |
| `@Builder.Default` | Campos com valor default garantidos pelo Lombok |
| Histórico automático | Toda mudança de status gera registro no `HistoricoCandidatura` |

---

## Autores
 
**Leticia Batista Silva**  
Engenheira Elétrica · Desenvolvedora Backend  
MBA em Engenharia de Software — USP
 
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com/in/leticia-batista-silva)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com/leticiabsilva03)
 
**Pedro Ventura Oliveira**  
Desenvolvedor Fullstack  
Especialização em Engenharia de Software — PUC Minas
 
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com/in/pedro-ventura-623426124)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=flat&logo=github&logoColor=white)](https://github.com/pdroVentu)
