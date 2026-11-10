# Security System — Java 21 / Spring Boot

Перенос [SecuritySystemv2](https://github.com/AldairRonin/SecuritySystemv2) с FastAPI
на Spring Boot с сохранением React-интерфейса и PostgreSQL. Анализ исходника,
контракты всех 21 операции и отличия: [docs/MIGRATION.md](docs/MIGRATION.md).

Проверено 10 ноября 2026: 14 unit + 7 PostgreSQL integration tests, Docker-запуск
трёх сервисов, браузерные сценарии с QR и сохранность данных после пересоздания
контейнеров. [Успешный CI](https://github.com/Buka2989/SecuritySystem/actions/runs/42821687984).

## Архитектура

- `backend/`: Java 21, Spring Boot 3.5, Web, JPA, Security, Validation, Maven,
  PostgreSQL JDBC, Argon2id, JWT HS256/384/512, Swagger/OpenAPI.
- `frontend/`: исходный React + TypeScript + Vite, роли PARENT, TRUSTED_PERSON,
  SECURITY, ADMIN. В контейнере статика обслуживается Nginx.
- `databases/`: схема предоставленной резервной копии без данных; восстановление
  custom-format backup в новый PostgreSQL volume.
- `docker-compose.yml`: postgres → backend:8080 → frontend:80.
  Браузер обращается к `/api/`, Nginx удаляет только этот префикс.

## Запуск

Нужны Git, Docker Engine/Desktop с Linux containers и Docker Compose v2.
Локальные Java, Maven, Node и PostgreSQL для Docker-запуска не нужны.

```sh
git clone https://github.com/Buka2989/SecuritySystem.git
cd SecuritySystem
cp .env.example .env
```

В PowerShell вместо `cp` можно использовать `Copy-Item .env.example .env`.
Настройте POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD, JWT_SECRET_KEY.
Замените все примерные секреты. Для HS256 нужно не менее 32 случайных байт,
HS384 — 48, HS512 — 64. Например: `openssl rand -hex 32`.
`JWT_EXPIRE_MINUTES` управляет сроком JWT; APP_TIME_ZONE — зоной naive timestamp.
Реальный `.env`, резервные копии, персональные данные исключены из Git.

### С существующей резервной копией

Перед **первым** запуском положите `security_backup.backup` в `databases/import/`.
Будет восстановлена полная копия данных, UUID, связей и существующих Argon2-паролей.
Оригинальная БД не изменяется. При восстановлении не применяются владельцы и GRANT
из исходной машины; владельцем становится POSTGRES_USER.

```sh
docker compose up -d --build
docker compose ps
```

### С пустой базой

Если backup отсутствует, создаются только исходные шесть таблиц. Для первого входа
укажите BOOTSTRAP_ADMIN_USERNAME и BOOTSTRAP_ADMIN_PASSWORD (от 12 символов).
Администратор создаётся **только если users пуста**. Существующие аккаунты и пароли
не обновляются. После первого запуска эти две переменные можно очистить.

- Интерфейс: http://localhost:3000
- API / health: http://localhost:8080/health
- Swagger: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

В Swagger выполните POST /auth/login, скопируйте access_token в Authorize.
Регистрация PARENT/TRUSTED_PERSON — POST /users/. Администратор тем же endpoint
создаёт SECURITY/ADMIN, создаёт учеников через POST /students/. В исходном React
нет форм регистрации и управления пользователями; API доступны через Swagger.
Доверенное лицо подаёт собственную связь с учеником на одобрение.

## Хранение и импорт данных

Именованный volume `postgres_data` переживает restart, down и повторную сборку.
Init выполняется только при пустом PGDATA. Не используйте `docker compose down -v`
для рабочей базы. Замена backup в import после первого запуска ничего не перезаписывает.
Для проверки другого backup создавайте отдельный Compose project с отдельными
портами/volume; не очищайте рабочий volume.

Рабочая схема берётся из backup, а не из устаревшего securitysys.sql. Hibernate
проверяет её с `ddl-auto=validate`, не создаёт и не меняет таблицы.
Перенос существующего физического PostgreSQL volume между major версиями запрещён;
используйте логический backup/restore. Compose использует PostgreSQL 18 и путь
volume `/var/lib/postgresql`, соответствующий этому образу.

```sh
# Интерактивный SQL внутри контейнера
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
# Резервная копия без бинарного перенаправления через PowerShell
docker compose exec postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc -f /tmp/security_backup.backup'
docker compose cp postgres:/tmp/security_backup.backup ./security_backup.backup
```

Порт PostgreSQL не публикуется наружу. Backend подключается к `postgres:5432`.
Для локального запуска вне Docker задайте DATABASE_URL вида
`jdbc:postgresql://127.0.0.1:5432/security_system` и отдельные POSTGRES_USER/PASSWORD.
URL формата SQLAlchemy `postgresql+psycopg://...` нужно заменить на JDBC URL.

## Остановка и диагностика

```sh
docker compose logs -f backend
docker compose logs -f postgres frontend
docker compose stop
docker compose start
docker compose down
```

Healthcheck PostgreSQL ждёт готовности БД; backend health выполняет SELECT 1;
frontend запускается после успешного backend health. Ошибка схемы останавливает
backend и требует проверки импортированной БД, а не автоматического изменения таблиц.

Для камеры на `localhost:3000` разрешите доступ браузеру. Доступ с другого устройства
по LAN требует HTTPS: браузер не предоставляет камеру по обычному HTTP IP-адресу.
Для развёртывания вне локальной машины настройте TLS на reverse proxy.

## Разработка и тестирование

Нужны JDK 21, Maven 3.9+, Node 22/npm, Docker для Testcontainers.

```sh
cd backend
mvn test
mvn verify
cd ../frontend
npm ci
npm run build
npm run dev
```

`mvn test` — unit-тесты JWT и QR-политики. `mvn verify` дополнительно запускает
ApiIT на настоящем PostgreSQL 18 через Testcontainers: auth, роли, validation,
поиск, связи, заявки, approve/reject/resubmit, soft delete/restore, QR expiry/revoke,
повторное и конкурентное сканирование, журналы. Отсутствие Docker не приводит
к молчаливому пропуску интеграционных тестов.

Для отдельного локального тестового PostgreSQL можно задать TEST_DATABASE_URL,
TEST_DATABASE_USER, TEST_DATABASE_PASSWORD. URL обязан оканчиваться `/security_test`;
база должна быть новой и пустой. Тесты создают схему и очищают таблицы между сценариями.
Никогда не указывайте рабочую БД.

GitHub Actions собирает и запускает все три Docker-сервиса, выполняет API и браузерные
тесты, проверяет данные после down/up и сохраняет отчёты. Текущий результат проверок
указан в [docs/TESTING.md](docs/TESTING.md); наличие конфигурации само по себе не
означает успешное прохождение.

## Известные ограничения исходной модели

Неизвестный QR нельзя сохранить в access_logs без изменения обязательного FK:
такие попытки пишутся в журнал backend; сканирования существующих QR — в PostgreSQL.
Даты сохранены без timezone для совместимости; APP_TIME_ZONE должен соответствовать
старому Python-процессу. Распознавания лиц и государственных паспортных интеграций нет.
