# java-backend-journey

Учебный **multi-module** проект для последовательного изучения Java backend-разработки:  
от базовых алгоритмов и структур данных до Hibernate, тестирования, Spring и микросервисов.

Проект развивается в формате, приближенном к production-подходу:

- Git flow (feature-ветки + pull request)
- слоистая архитектура (DAO / Service)
- явное управление транзакциями
- инфраструктурные proxy (transactions / logging / masking)
- постепенное усложнение архитектуры и тестового покрытия

---

## 📦 Структура проекта

```
java-backend-journey
├── module-1-git-algorithms
│   └── Custom HashMap + unit tests
│
├── module-2-user-service
│   ├── Console CRUD application
│   ├── Hibernate ORM (без Spring)
│   ├── PostgreSQL
│   ├── DAO / Service layers
│   ├── Transactional / Logging / Masking proxies
│   └── Docker-based infrastructure
│
├── module-3-user-service-tests
│   ├── Unit tests: UserService (Mockito + JUnit 5)
│   └── Integration tests: UserDao (Testcontainers PostgreSQL + Hibernate)
│
├── docker-compose.yml
└── README.md
```

---

## 🔹 Module 1 — Git, алгоритмы и структуры данных

**Содержание:**
- собственная реализация `HashMap<K, V>`
- поддержка:
  - `put`, `get`, `remove`, `size`, `clear`
  - `null` key
  - resize с rehashing
- unit-тесты (JUnit 5)

**Цели модуля:**
- понять внутреннее устройство `HashMap`
- закрепить работу с Git (ветки, PR, merge)
- практика TDD / unit-тестирования

---

## 🔹 Module 2 — User Service (Hibernate + PostgreSQL)

**Описание:**  
Консольное Java-приложение без Spring, реализующее CRUD-операции над пользователями
с использованием Hibernate и PostgreSQL.

Приложение построено по слоистой архитектуре и имитирует backend-сервис
без web-контроллеров (CLI вместо REST).

### 🧩 Архитектура

```
Main (CLI)
  ↓
Service layer (UserService)
  ↓
DAO interface (UserDao)
  ↓
Proxy chain:
    ├─ TransactionalUserDaoProxy
    ├─ LoggingUserDaoProxy
    └─ MaskingUserDaoProxy
  ↓
Hibernate / PostgreSQL
```

**Ключевая идея:**
- **Service слой** содержит бизнес-валидации и маппинг ошибок
- **DAO слой** отвечает только за доступ к данным
- **Транзакции, логирование и маскирование** вынесены в отдельные proxy
- Каждая proxy имеет **одну зону ответственности**

### ⚙️ Функциональность

- CRUD-операции над сущностью `User`
- валидация входных данных на уровне Service
- контроль уникальности email (через constraint + exception mapping)
- транзакционность на уровне DAO (через proxy)
- логирование времени выполнения DAO-методов
- логирование `rowsAffected` для update / delete
- маскирование email в логах
- консольное меню управления пользователями

### 🧱 Сущность User

```
User {
  Long id
  String name
  String email
  Integer age
  LocalDateTime createdAt
}
```

- `id` — генерируется БД
- `createdAt` — инициализируется через `@PrePersist`
- `email` — уникален (DB constraint)
- `age` — обязательное поле с валидацией диапазона

---

## 🔹 Module 3 — Тесты для User Service

Отдельный модуль с тестами для `module-2-user-service`.

### ✅ Unit tests (Service)

- `UserServiceImplTest`
- Mockito + JUnit 5
- проверяются:
  - валидации входных данных (name/email/age/id)
  - корректное взаимодействие с `UserDao`
  - маппинг ошибок (например, duplicate email → `IllegalStateException`)

### ✅ Integration tests (DAO)

- `UserDaoImplIT`
- Hibernate + Testcontainers PostgreSQL
- проверяются:
  - `save/findById/findByEmail/updateById/delete`
  - нарушение уникальности email (SQLState `23505`)

> Для интеграционных тестов необходим запущенный Docker Engine (Testcontainers поднимает Postgres сам).  
> `docker-compose.yml` для Module 2 при этом не нужен.

---

## 🐘 Запуск PostgreSQL (Docker) для разработки (Module 2)

```bash
docker compose up -d
```

PostgreSQL будет доступен по адресу:

```
jdbc:postgresql://localhost:5432/user_service
```

Параметры подключения задаются через `.env` файл.

---

## ▶️ Запуск приложения (Module 2)

### Из IntelliJ IDEA
Запуск `Main` как обычного Java Application.

### Из консоли
```bash
./gradlew :module-2-user-service:run
```

---

## 🧪 Запуск тестов

### Все тесты во всех модулях
```bash
./gradlew test
```

### Только тесты модуля 3
```bash
./gradlew :module-3-user-service-tests:test
```

### Перегенерировать HTML-отчёт (удобно для скриншотов)
```bash
./gradlew :module-3-user-service-tests:cleanTest :module-3-user-service-tests:test --rerun-tasks
```

HTML-отчёт:
```
module-3-user-service-tests/build/reports/tests/test/index.html
```

---

## 🛠 Используемые технологии

- Java 21
- Gradle (multi-module)
- Hibernate ORM 7
- Jakarta Persistence
- PostgreSQL 15
- SLF4J + Logback
- Docker / Docker Compose (локальная инфраструктура)
- JUnit 5, Mockito (unit tests)
- Testcontainers (integration tests)

---

## 📌 Примечания

- Проект учебный, но архитектура и подходы приближены к production.
- Module 2 намеренно реализован **без Spring** для глубокого понимания Hibernate,
  транзакций и инфраструктурных аспектов.
- Module 3 выделен отдельно, чтобы тесты были изолированы от кода приложения и
  развивались как самостоятельный слой качества.

---

## 🇬🇧 English (short)

Educational multi-module Java backend project.

Includes:
- custom data structures (HashMap)
- console CRUD application with Hibernate + PostgreSQL (no Spring)
- layered architecture (DAO / Service)
- infrastructure proxy chain (transactions, logging, masking)
- separate tests module:
  - unit tests (Mockito + JUnit 5)
  - integration tests (Testcontainers PostgreSQL + Hibernate)

The project evolves step by step, following production-like practices.
