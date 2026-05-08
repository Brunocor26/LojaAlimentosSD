# LojaSD — Guia para o Claude

## Idioma Obrigatório
Toda a comunicação, comentários de código e documentação devem ser em **Português de Portugal (PT-PT)**.

## Regras Absolutas de Git
- **NUNCA** fazer `git push` sem autorização explícita do utilizador
- **NUNCA** fazer `git commit` sem autorização explícita do utilizador
- **NUNCA** adicionar "Co-Authored-By: Claude" ou qualquer referência ao Claude nas mensagens de commit
- **NUNCA** mencionar "CLAUDE.md" ou "Claude" em mensagens de commit — nem como ficheiro adicionado, nem como ferramenta usada
- Não usar flags `--no-verify`, `--force` sem autorização explícita

## Fluxo de Trabalho Obrigatório
1. Analisar sempre o estado atual (`git status`, ler ficheiros relevantes) antes de qualquer alteração
2. Nunca criar ficheiros novos sem verificar se já existe implementação reutilizável
3. Propor alterações e aguardar confirmação para mudanças de arquitectura
4. Testar localmente antes de declarar qualquer tarefa concluída

## Projecto
- **Disciplina:** Sistemas Distribuídos — Trabalho Prático 2
- **Tema:** T5 — Loja de Produtos Alimentares
- **Equipa:** Bruno, Henrique, Francisco
- **Avaliação:** 7 valores (35% nota final)
  - Código da aplicação: 4 valores
  - Relatório técnico (LaTeX/PDF): 2 valores
  - Apresentação e defesa: 1 valor

## Stack Técnica
- Java 25 + Spring Boot 4.1.0-RC1
- Spring Security (autenticação por sessões HTTP)
- Spring Data JPA + Hibernate
- MariaDB (SGBD recomendado pelo professor)
- Maven (usar `./mvnw`)
- HTML/CSS/JS vanilla (frontend, sem frameworks)
- DBBeaver (gestão GUI da base de dados)

## Estrutura de Pacotes
```
src/main/java/ubi/sd/lojasd/
├── config/        — SecurityConfig, CorsConfig
├── model/         — Entidades JPA (Produto, Categoria, Cliente, Venda, ItemVenda, Fatura, Stock)
├── repository/    — Interfaces JpaRepository
├── service/       — Lógica de negócio (nunca lógica nos controllers)
├── controller/    — Endpoints REST (@RestController)
└── dto/           — Records Java para entrada/saída de dados
```

## Base de Dados
- **SGBD:** MariaDB (porta padrão 3306)
- **Base de dados:** `lojasd_db`
- **Utilizador:** `lojasd_user`
- **Credenciais:** Sempre em ficheiro `.env` na raiz (NUNCA em application.properties)
- **Gestão visual:** DBBeaver
- **Schema:** Criado automaticamente pelo Hibernate (`ddl-auto=update`)

## Comandos Úteis
```bash
# Iniciar MariaDB
brew services start mariadb

# Parar MariaDB
brew services stop mariadb

# Correr a aplicação
./mvnw spring-boot:run

# Correr testes
./mvnw test

# Aceder à BD via terminal
mysql -u lojasd_user -p lojasd_db

# Aceder como root
mysql -u root
```

## Endpoints da API (resumo)
| Método | Endpoint | Acesso | Descrição |
|--------|----------|--------|-----------|
| GET | /api/produtos | Público | Listar produtos ativos |
| GET | /api/produtos/{id} | Público | Detalhe de produto |
| GET | /api/produtos/categoria/{id} | Público | Produtos por categoria |
| POST | /api/produtos | ADMIN | Criar produto |
| PUT | /api/produtos/{id} | ADMIN | Atualizar produto |
| DELETE | /api/produtos/{id} | ADMIN | Desativar produto |
| GET | /api/categorias | Público | Listar categorias |
| POST | /api/auth/registar | Público | Registar cliente |
| POST | /api/auth/login | Público | Fazer login |
| POST | /api/auth/logout | Autenticado | Terminar sessão |
| GET | /api/auth/me | Autenticado | Dados do utilizador atual |
| GET | /api/carrinho | Autenticado | Ver carrinho |
| POST | /api/carrinho/adicionar/{id} | Autenticado | Adicionar ao carrinho |
| DELETE | /api/carrinho/remover/{id} | Autenticado | Remover do carrinho |
| POST | /api/vendas | Autenticado | Finalizar compra |
| GET | /api/vendas/minhas | Autenticado | Historial de compras |
| GET | /api/faturas/{vendaId} | Autenticado | Ver fatura |
| GET | /api/estatisticas/dashboard | ADMIN | Dashboard de estatísticas |

## Requisitos do Enunciado (checklist)
- [ ] CRUD de produtos com categorias
- [ ] Registo e autenticação de clientes
- [ ] Gestão de sessões HTTP (Spring Security)
- [ ] Carrinho de compras (persistência em sessão)
- [ ] Compras online (apenas clientes registados)
- [ ] Gestão de stocks (baixa automática na venda)
- [ ] Faturação com IVA (emitir fatura por venda)
- [ ] Estatísticas: produtos mais vendidos, melhores clientes, faturação por período
- [ ] Segurança: validação de inputs, controlo de acesso por papel (ADMIN/CLIENTE)
- [ ] Relatório técnico em LaTeX
- [ ] Scripts SQL de criação e inicialização da BD
- [ ] README completo com instruções de instalação e execução local
