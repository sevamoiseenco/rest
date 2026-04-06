# REST

REST-сервис для управления задачами (Kotlin + Spring Boot + Reactor + JdbcClient + native SQL).

## Требования

- JDK 17+
- PowerShell (Windows) или bash

## Запуск

### Windows (PowerShell)

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
.\gradlew.bat bootRun
```

### Linux / macOS

```bash
export JAVA_HOME=/path/to/jdk-17
./gradlew bootRun
```

После запуска сервис доступен по адресу:

- `http://localhost:8080`

## Запуск тестов

### Windows (PowerShell)

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
.\gradlew.bat test
```

### Linux / macOS

```bash
./gradlew test
```

## Основные endpoint'ы

- `POST /api/tasks` — создать задачу
- `GET /api/tasks/{id}` — получить задачу по id
- `GET /api/tasks?page=0&size=10&status=NEW` — список задач с пагинацией и фильтром
- `PATCH /api/tasks/{id}/status` — обновить статус
- `DELETE /api/tasks/{id}` — удалить задачу

## Примеры запросов

### Создать задачу

```http
POST /api/tasks
Content-Type: application/json

{
  "title": "Prepare report",
  "description": "Monthly financial report"
}
```

### Обновить статус

```http
PATCH /api/tasks/1/status
Content-Type: application/json

{
  "status": "DONE"
}
```
