# UnitHub API

O UnitHub é uma plataforma web e mobile projetada para o gerenciamento de eventos acadêmicos, funcionando como um portal que facilita a divulgação de eventos, formulários, ações e outras atividades. Os alunos podem solicitar a publicação de posts na plataforma, e, podem ser aprovados pelos alunos representantes ou organizadores. O objetivo é otimizar a inscrição, gestão e divulgação, auxiliando a equipe de marketing e a gestão de eventos da universidade.

## Tecnologias Utilizadas

- Java 17  
- Spring Boot 3.4.4  
- MySQL  
- Amazon S3

---

## Passos para Execução

### 1. Criar o bucket S3 no LocalStack

LocalStack é utilizado para emular serviços AWS localmente, como o S3. Após iniciar o LocalStack com Docker, crie um bucket S3 chamado `s3unithub`.

Execute o seguinte comando para criar o bucket no LocalStack:

```bash
docker exec -it localstack awslocal s3 mb s3://s3unithub
```

### 2. Executar o Mysql no Docker
Agora, vamos configurar o MySQL utilizando o Docker Compose.

1.Navegue até o diretório onde o arquivo `docker-compose.yml` do MySQL está localizado (caso ainda não tenha esse arquivo, consulte o exemplo abaixo).

```bash
cd docker
```
2. Execute o Docker Compose para iniciar o MySQL:

```bash
docker compose up
```

### 3. Gerar o Par de Chaves (Privada e Pública)
Você precisa gerar um par de chaves RSA usando o OpenSSL. Se não tiver o OpenSSL instalado, você pode baixar a versão apropriada para o seu sistema operacional aqui.

Execute os seguintes comandos no terminal para gerar a chave privada (app.key) e a chave pública

```
src/
 └── main/
      └── resources/
           ├── app.key
           └── app.pub
```

Gerar a chave privada (app.key):
```bash
openssl genpkey -algorithm RSA -out app.key -pkeyopt rsa_keygen_bits:2048
```

Gerar a chave pública (app.pub) a partir da chave privada:
```bash
openssl rsa -pubout -in app.key -out app.pub
```

### 4. Executar o Projeto Spring Boot
Agora que o MySQL e o LocalStack estão em execução, é hora de rodar o projeto Spring Boot.

1- Certifique-se de que todas as dependências do projeto estão baixadas e configuradas corretamente com Maven.

```bash
mvn clean install
```

2- No diretório raiz do projeto, execute os seguintes comandos para compilar e executar o Spring Boot:

```bash
mvn spring-boot:run
```

### 4. Acessar a Aplicação
Após o Spring Boot ser iniciado, a aplicação estará disponível em http://localhost:8080.

---

## Categorias e Roles da API

### Categorias de Cursos e Eventos

Cursos e eventos podem pertencer a uma das 5 categorias abaixo:

| ID  | Categoria   |
|-----|-------------|
| 1L  | Tecnologia  |
| 2L  | Saúde       |
| 3L  | Engenharia  |
| 4L  | Humanas     |
| 5L  | Exatas      |

---

### Tipos de Usuário (Roles)

A plataforma possui 4 tipos de usuários com permissões diferentes:

| ID  | Role                | Permissões |
|-----|---------------------|------------|
| 1L  | `ADMIN`             | Gerencia todos os usuários, cursos e posts de eventos da plataforma |
| 2L  | `ORGANIZADOR`       | Gerencia post de eventos, inscrições, promove alunos a representantes e cria eventos oficiais |
| 3L  | `ALUNO`             | Cria eventos não oficiais (precisa de aprovação), inscreve-se em eventos, acessa o feed |
| 4L  | `ALUNO_REPRESENTANTE` | Aprova ou reprova eventos do seu curso |

> **Observação**:  
> Eventos criados por alunos são inicialmente inativos e precisam ser aprovados por um representante do curso ou organizador.

---
## Documentação da API
> Você pode consultar os endpoints pelo swagger http://localhost:8080/swagger-ui/index.html

### Autenticação (`AuthControllers`)

| Método | Rota | Descrição | Requisição | Autenticação | Status |
|--------|------|-----------|------------|---------------|--------|
| POST | `/login` | Autentica um usuário | `email`, `senha` | ❌ | 200 |
| POST | `/register` | Cadastra novo usuário | `email`, `senha`, `confirmaçãoSenha`, `telefone`, `nome`, `idCurso` | ❌ | 201 |
| POST | `/recover-password` | Envia e-mail com token de recuperação | `email` | ❌ | 200 |
| PATCH | `/reset-password` | Altera a senha com token | `novaSenha` | ✅ JWT | 200 |

#### 🛑 Erros Comuns

| Código | Mensagem | Causa |
|--------|----------|--------|
| 401 | Invalid email or password | Email ou senha incorretos |
| 401 | Invalid email | Email inválido na recuperação |
| 400 | Passwords do not match | Senhas diferentes no cadastro |
| 422 | Email already registered | Email já cadastrado |
| 500 | Role 'ALUNO' not registered | Role padrão não registrada |
| 404 | Course not found with ID | Curso não encontrado |

---

### Usuários (`UserControllers`)

| Método | Rota | Descrição | Requisição | Autenticação | Status |
|--------|------|-----------|------------|--------------|--------|
| PATCH | `/users/change-role` | Altera a role do usuário | `idUsuario`, `idRole` | ✅ JWT | 200 |
| GET | `/users/role/{roleId}` | Lista usuários por role | `roleId` | ✅ JWT | 200 |
| DELETE | `/users/{userId}` | Deleta usuário | `userId` | ✅ JWT | 204 |
| GET | `/users/profile` | Mostra informações do usuário autenticado | - | ✅ JWT | 200 |
| PATCH | `/users/profile` | Mostra informações do usuário autenticado | `email`, `senha`, `confirmaçãoSenha`, `telefone` | ✅ JWT | 200 |

#### 🛑 Erros Comuns

| Código | Mensagem | Causa |
|--------|----------|--------|
| 403 | You don't have permission to perform this action | Permissão negada |
| 404 | User not found | Usuário não encontrado |
| 404 | Role not found | Role não encontrada |
| 403 | Cannot change admin user | Não é possível alterar um admin |
| 403 | Cannot assign admin role | Não é possível atribuir role de admin |
| 403 | Cannot delete admin user | Não é possível deletar usuário admin |

---

### Cursos (`CourseControllers`)

| Método | Rota | Descrição | Requisição | Autenticação | Status |
|--------|------|-----------|------------|--------------|--------|
| GET | `/courses/categories` | Lista categorias disponíveis | - | ❌ | 200 |
| POST | `/courses` | Cria novo curso | `idCategoria`, `nomeCurso` | ✅ JWT | 201 |
| GET | `/courses` | Lista todos os cursos | - | ❌ | 200 |
| PATCH | `/courses/{cursoId}` | Atualiza Evento | `cursoId` | ✅ JWT | 200 |
| DELETE | `/courses/{cursoId}` | Deleta um curso | `cursoId` | ✅ JWT | 204 |

#### 🛑 Erros Comuns

| Código | Mensagem | Causa |
|--------|----------|--------|
| 403 | You don't have permission | Ação sem permissão |
| 404 | Course not found | Curso não encontrado |

---

### Eventos (`EventControllers`)

| Método | Rota | Descrição | Requisição | Autenticação | Status |
|--------|------|-----------|------------|--------------|--------|
| POST | `/events` | Cria novo evento | `titulo`, `descrição`, `dataHora`, `local`, `idCategoria`, `maxParticipantes`, `imagens` | ✅ JWT | 201 |
| GET | `/events/self-posts` | Lista eventos criados pelo usuário | - | ✅ JWT | 200 |
| PATCH | `/events/{eventId}` | Atualiza evento | `eventId`, dados do evento | ✅ JWT | 200 |
| DELETE | `/events/{eventId}` | Deleta evento | `eventId` | ✅ JWT | 204 |
| GET | `/events/feed` | Lista eventos ativos | - | ❌ | 200 |
| GET | `/events/feed-by-course` | Lista eventos por categoria do curso do usuário autenticado | - | ✅ JWT | 200 |
| GET | `/events/feed-by-course-creator` | Lista eventos criados por usuários do mesmo curso que usuário autenticado  | - | ✅ JWT | 200 |
| POST | `/events/subscribe/{eventId}` | Inscreve usuário no evento | `eventId` | ✅ JWT | 200 |
| POST | `/events/unsubscribe/{eventId}` | Remove inscrição do evento | `eventId` | ✅ JWT | 200 |
| GET | `/events/subscribed` | Lista eventos inscritos | - | ✅ JWT | 200 |

#### 🛑 Erros Comuns

| Código | Mensagem | Causa |
|--------|----------|--------|
| 400 | The event can have a maximum of 4 images | Limite de imagens excedido |
| 404 | Event not found | Evento inexistente |
| 403 | You don't have permission to update this active event | Não pode editar evento aprovado |
| 403 | You don't have permission to delete this event | Permissão negada |
| 400 | Isn't possible to subscribe event | Inscrição não permitida |
| 409 | User is already enrolled in the event | Já está inscrito |
| 400 | Maximum participants exceeded | Vagas esgotadas |
| 409 | User isn't enrolled in the event | Não está inscrito |

---

### Gerenciamento de Eventos (`EventManagmentControllers`)

| Método | Rota | Descrição | Requisição | Autenticação | Status |
|--------|------|-----------|------------|--------------|--------|
| GET | `/managment/subscribers/{eventId}` | Lista usuários inscritos no evento | `eventId` | ✅ JWT | 200 |
| PATCH | `/managment/{eventId}` | Aprova evento (ativa) | `eventId` | ✅ JWT | 200 |
| DELETE | `/managment/{eventId}` | Rejeita evento (motivo no corpo) | `eventId`, `motivo` | ✅ JWT | 204 |

#### 🛑 Erros Comuns

| Código | Mensagem | Causa |
|--------|----------|--------|
| 404 | Event not found | Evento não encontrado |
| 404 | No subscribers found for this event | Nenhum inscrito |
| 403 | You don't have permission | Permissão negada |
| 403 | You don't have permission to accept/reject events outside your course | Evento de outro curso |

---

> Esta API é parte do sistema UnitHub, voltado para gerenciamento de cursos, usuários e eventos universitários.
