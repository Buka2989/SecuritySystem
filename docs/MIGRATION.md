# Анализ и решения переноса

Исходник: https://github.com/AldairRonin/SecuritySystemv2, commit
`a77396d6277f1415b2dbc5ac9d5affd9ab2676fb`.

## Исходная архитектура

FastAPI + синхронный SQLAlchemy/psycopg; логика находилась в routers.
JWT содержит `sub` (UUID), `role`, `exp`. Пароли — Argon2id через pwdlib.
React 18/TypeScript/Vite содержит семь страниц: login, dashboard, add-child,
выбор родства, карточка ребёнка, admin, security. Используется настоящий fetch,
вопреки устаревшему README про mock-данные. QR рисуется в браузере qrcode.react,
сканируется html5-qrcode; backend выдаёт только криптографический токен.

В опубликованном исходнике нет Dockerfile, docker-compose.yml, nginx.conf и
корневого .env.example. Инфраструктура Java-версии создана заново по заданным требованиям.

## База данных

Шесть таблиц: users, students, guardian_student, verification_requests, qr_codes,
access_logs. Сохранены UUID, имена, длины varchar, timestamp WITHOUT time zone,
NOT NULL, CHECK, UNIQUE и FK. В JPA UUID связей представлены явными полями;
целостность обеспечивается исходными FK PostgreSQL. Нет каскадных удалений.
`ddl-auto=validate`, автоматические миграции и SQL initialization Spring выключены.

`securitysys.sql` из репозитория и предоставленный SQL — устаревшая схема:
phone/email вместо username; отсутствуют is_active и CANCELLED.
Предоставленный custom-format backup соответствует текущему Python-коду:
username varchar(50), is_active boolean, CANCELLED разрешён.
`databases/schema.sql` содержит только DDL этой резервной копии, включая UUID defaults.
Содержимое персональных данных и хеши паролей в Git не включаются.

Для базы из backup миграция не требуется. Старый phone/email SQL нельзя выдавать
за совместимую рабочую схему. Переход с него требует отдельного согласования:
добавить username (без удаления phone/email), согласовать уникальные логины,
заполнить их, добавить is_active DEFAULT true, расширить CHECK для CANCELLED.
Также необходимо решить, чем заполнять обязательный phone для новой регистрации.
Этот переход не выполняется автоматически.

## Контракты исходных endpoints

Коллекции поддерживают URL со слешем и без него. JSON snake_case, UUID строки,
даты ISO-8601 без часового смещения как в Python. Обычные ошибки `{ "detail": "..." }`;
ошибки валидации 422 с массивом detail, loc, msg, type. Тексты bean validation
не побайтово идентичны Pydantic. Создание возвращает 201, soft delete — 204.

| Метод | URL | Доступ после согласованного исправления |
|---|---|---|
| GET | / | публичный |
| GET | /health | публичный, SELECT 1 |
| POST | /auth/login | публичный |
| GET | /auth/me | JWT |
| GET | /users/ | ADMIN |
| POST | /users/ | публично PARENT/TRUSTED_PERSON с ACTIVE; остальные — ADMIN |
| GET | /students/ | JWT |
| GET | /students/search?full_name=&class_name= | JWT |
| POST | /students/ | ADMIN |
| GET | /guardian-students/ | PARENT/TRUSTED_PERSON, свои активные связи |
| POST | /guardian-students/ | PARENT/TRUSTED_PERSON |
| DELETE | /guardian-students/{id} | владелец связи |
| GET | /verification-requests/ | ADMIN, все статусы, включая историю |
| POST | /verification-requests/ | владелец связи |
| PATCH | /verification-requests/{id}/approve | ADMIN |
| PATCH | /verification-requests/{id}/reject | ADMIN |
| GET | /qr-codes/ | PARENT/TRUSTED_PERSON, собственные QR |
| POST | /qr-codes/ | владелец подтверждённой активной связи |
| POST | /qr-codes/verify | SECURITY |
| PATCH | /qr-codes/{id}/revoke | владелец QR |
| GET | /access-logs/ | ADMIN/SECURITY |

Добавлены совместимые расширения для требований, не реализованных в исходнике:
GET /students/{id}, PATCH /users/me (`full_name`), PATCH /users/{id}/status (`status`, ADMIN).
Регистрация уже существовала как POST /users/, отдельного /auth/register не было.
Отдельного приглашения доверенного лица родителем или паспортной проверки не было:
TRUSTED_PERSON сам создаёт связь и проходит такое же подтверждение администратора.
Эти отсутствующие процессы не подменены фиктивными реализациями.

## Обнаруженные ошибки и принятые решения

1. Публичное создание ADMIN/SECURITY и учеников, публичный список учеников.
   Пользователь согласовал ограничения доступа. Два запроса списка учеников
   frontend теперь передают уже имеющийся JWT.
2. Python проверял QR без блокировки. Java блокирует guardian_student, затем
   qr_codes через SELECT FOR UPDATE в одной транзакции. Проверка статуса,
   USED/used_at и access_log атомарны. При ошибке записи журнала QR не расходуется.
   На повторном сканировании после ожидания блокировки читается актуальный статус.
3. Soft delete не блокировал ранее выпущенный QR. Теперь он отзывает активные QR,
   отменяет PENDING-заявки. При восстановлении сохраняется UUID связи и история,
   требуется новое подтверждение. Все изменения связи используют ту же блокировку.
4. Заблокированный родитель/неактивный ученик больше не проходят по ранее выданному QR.
5. Срок QR проверяется сервером; 1–1440 минут, default 30, frontend просит 15.
   Истечение наступает при expires_at <= now, а не только при строго меньшем времени.
6. TRUSTED_PERSON не перенаправлялся после логина; исправлены маршруты без смены дизайна.
   Добавлены тип и подпись CANCELLED. Удалён вывод секретного QR-токена в console.log.
7. access_logs.qr_code_id NOT NULL с FK не позволяет сохранить неизвестный токен.
   Сохранено поведение таблицы Python: INVALID_QR возвращается, попытка пишется
   в серверный журнал без самого токена. Все сканирования существующих QR сохраняются
   в PostgreSQL. Для записи неизвестных QR в таблицу нужна согласованная миграция:
   разрешить NULL в qr_code_id; FK для ненулевых значений остаётся. Не применена.
8. Naive timestamps исходника не содержат timezone. APP_TIME_ZONE должен совпадать
   с timezone прежнего Python-процесса (default Asia/Qyzylorda). Исходные даты не
   пересчитываются. Для браузеров в другой зоне нужен отдельный согласованный переход
   на offset-aware API; текущий контракт сохранён.

## Слои Java

controller — маршруты/валидация/роли; service — транзакционная бизнес-логика;
repository — JPA и блокировки; entity — шесть существующих таблиц;
dto — явные request/response records, пароль не сериализуется;
security — подпись и проверка JWT, текущие роли и статус из БД;
config — CORS, Clock, Swagger и необязательное создание первого администратора;
exception — совместимый detail envelope.
