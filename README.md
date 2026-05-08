# LojaSD — Loja de Produtos Alimentares

Trabalho Prático 2 de Sistemas Distribuídos — UBI  
**Equipa:** Bruno, Henrique, Francisco

---

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e em execução
- Java 21 ([download](https://www.oracle.com/java/technologies/downloads/#java21))

---

## Instalação e execução

### 1. Clonar o repositório

```bash
git clone <url-do-repositorio>
cd SD-trabalho-pratico2
```

### 2. Arrancar a base de dados

```bash
docker compose up -d
```

Inicia o MariaDB num container Docker. As tabelas e dados iniciais são criados automaticamente pela aplicação.

### 3. Arrancar a aplicação

```bash
./mvnw spring-boot:run
```

Disponível em: **http://localhost:8080**

---

## Comandos úteis

```bash
# Iniciar a BD
docker compose up -d

# Parar a BD
docker compose down

# Ver estado
docker compose ps

# Aceder à BD via terminal
docker exec -it lojasd-db mariadb -u lojasd_user -plojasd_password_local lojasd_db
```

---

## DBBeaver — ligação à base de dados

| Campo    | Valor                   |
|----------|-------------------------|
| Host     | `localhost`             |
| Port     | `3306`                  |
| Database | `lojasd_db`             |
| User     | `lojasd_user`           |
| Password | `lojasd_password_local` |

---

## Credenciais locais

O ficheiro `src/main/resources/application-local.properties` está no `.gitignore`.  
Se não existir na tua máquina após clonar, cria-o com:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/lojasd_db
spring.datasource.username=lojasd_user
spring.datasource.password=lojasd_password_local
```

As credenciais são as mesmas do `docker-compose.yml`.
