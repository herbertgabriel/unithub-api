# 📚 UnitHub API

**Base URL:**  
`http://localhost:8080`

## ⚙️ Tecnologias Utilizadas

- Java 17  
- Spring Boot 3.4.4  
- MySQL  
- Amazon S3  

---

## 🧭 Categorias e Roles

### 📚 Categorias de Cursos e Eventos

Cursos e eventos podem pertencer a uma das 5 categorias abaixo:

| ID  | Categoria   |
|-----|-------------|
| 1L  | Tecnologia  |
| 2L  | Saúde       |
| 3L  | Engenharia  |
| 4L  | Humanas     |
| 5L  | Exatas      |

---

### 👥 Tipos de Usuário (Roles)

A plataforma possui 4 tipos de usuários com permissões diferentes:

| ID  | Role                | Permissões |
|-----|---------------------|------------|
| 1L  | `ADMIN`             | Gerencia todos os usuários, cursos e posts de eventos da plataforma |
| 2L  | `ORGANIZADOR`       | Gerencia post de eventos, inscrições, promove alunos a representantes e cria eventos oficiais |
| 3L  | `ALUNO`             | Cria eventos não oficiais (precisa de aprovação), inscreve-se em eventos, acessa o feed |
| 4L  | `ALUNO_REPRESENTANTE` | Aprova ou reprova eventos do seu curso |

> ✅ **Observação**:  
> Eventos criados por alunos são inicialmente inativos e precisam ser aprovados por um representante do curso ou organizador.

---

## 🔐 Autenticação (`AuthControllers`)

| Método | Rota | Descrição | Requisição | Autenticação | Status |
|--------|------|-----------|------------|---------------|--------|
| POST | `/login` | Autentica um usuário | `email`, `senha` | ❌ | 200 |
| POST | `/register` | Cadastra novo usuário | `email`, `senha`, `confirmaçãoSenha`, `telefone`, `nome`, `idCurso` | ❌ | 201 |
| POST | `/recover-password` | Envia e-mail com token de recuperação | `email` | ❌ | 200 |
| PATCH | `/reset-password` | Altera a senha com token | `novaSenha` | ✅ JWT | 200 |

### 🛑 Erros Comuns

| Código | Mensagem | Causa |
|--------|----------|--------|
| 401 | Invalid email or password | Email ou senha incorretos |
| 401 | Invalid email | Email inválido na recuperação |
| 400 | Passwords do not match | Senhas diferentes no cadastro |
| 422 | Email already registered | Email já cadastrado |
| 500 | Role 'ALUNO' not registered | Role padrão não registrada |
| 404 | Course not found with ID | Curso não encontrado |

---

## 👤 Usuários (`UserControllers`)

| Método | Rota | Descrição | Requisição | Autenticação | Status |
|--------|------|-----------|------------|--------------|--------|
| PATCH | `/users/change-role` | Altera a role do usuário | `idUsuario`, `idRole` | ✅ JWT | 200 |
| GET | `/users/role/{roleId}` | Lista usuários por role | `roleId` | ✅ JWT | 200 |
| DELETE | `/users/{userId}` | Deleta usuário | `userId` | ✅ JWT | 204 |

### 🛑 Erros Comuns

| Código | Mensagem | Causa |
|--------|----------|--------|
| 403 | You don't have permission to perform this action | Permissão negada |
| 404 | User not found | Usuário não encontrado |
| 404 | Role not found | Role não encontrada |
| 403 | Cannot change admin user | Não é possível alterar um admin |
| 403 | Cannot assign admin role | Não é possível atribuir role de admin |
| 403 | Cannot delete admin user | Não é possível deletar usuário admin |

---

## 📚 Cursos (`CourseControllers`)

| Método | Rota | Descrição | Requisição | Autenticação | Status |
|--------|------|-----------|------------|--------------|--------|
| GET | `/courses/categories` | Lista categorias disponíveis | - | ❌ | 200 |
| POST | `/courses` | Cria novo curso | `idCategoria`, `nomeCurso` | ✅ JWT | 201 |
| GET | `/courses` | Lista todos os cursos | - | ❌ | 200 |
| DELETE | `/courses/{cursoId}` | Deleta um curso | `cursoId` | ✅ JWT | 204 |

### 🛑 Erros Comuns

| Código | Mensagem | Causa |
|--------|----------|--------|
| 403 | You don't have permission | Ação sem permissão |
| 404 | Course not found | Curso não encontrado |

---

## 📅 Eventos (`EventControllers`)

| Método | Rota | Descrição | Requisição | Autenticação | Status |
|--------|------|-----------|------------|--------------|--------|
| POST | `/events` | Cria novo evento | `titulo`, `descrição`, `dataHora`, `local`, `idCategoria`, `maxParticipantes`, `imagens` | ✅ JWT | 201 |
| GET | `/events/self-posts` | Lista eventos criados pelo usuário | - | ✅ JWT | 200 |
| PATCH | `/events/{eventId}` | Atualiza evento | `eventId`, dados do evento | ✅ JWT | 200 |
| DELETE | `/events/{eventId}` | Deleta evento | `eventId` | ✅ JWT | 204 |
| GET | `/events/feed` | Lista eventos ativos | - | ❌ | 200 |
| GET | `/events/feed-by-course` | Lista eventos do curso do usuário | - | ✅ JWT | 200 |
| POST | `/events/subscribe/{eventId}` | Inscreve usuário no evento | `eventId` | ✅ JWT | 200 |
| POST | `/events/unsubscribe/{eventId}` | Remove inscrição do evento | `eventId` | ✅ JWT | 200 |
| GET | `/events/subscribed` | Lista eventos inscritos | - | ✅ JWT | 200 |

### 🛑 Erros Comuns

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

## 🧑‍💼 Gerenciamento de Eventos (`EventManagmentControllers`)

| Método | Rota | Descrição | Requisição | Autenticação | Status |
|--------|------|-----------|------------|--------------|--------|
| GET | `/managment/subscribers/{eventId}` | Lista usuários inscritos no evento | `eventId` | ✅ JWT | 200 |
| PATCH | `/managment/{eventId}` | Aprova evento (ativa) | `eventId` | ✅ JWT | 200 |
| DELETE | `/managment/{eventId}` | Rejeita evento (motivo no corpo) | `eventId`, `motivo` | ✅ JWT | 204 |

### 🛑 Erros Comuns

| Código | Mensagem | Causa |
|--------|----------|--------|
| 404 | Event not found | Evento não encontrado |
| 404 | No subscribers found for this event | Nenhum inscrito |
| 403 | You don't have permission | Permissão negada |
| 403 | You don't have permission to accept/reject events outside your course | Evento de outro curso |

---

> ℹ️ Esta API é parte do sistema UnitHub, voltado para gerenciamento de cursos, usuários e eventos universitários.
