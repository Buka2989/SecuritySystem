# Проверки

Дата: 10 ноября 2026.

Локально, JDK 21.0.13, PostgreSQL 18.0:

- Maven package и 14 unit-тестов — успешно, без пропусков. Включены независимо
  сгенерированные Python JWT и Argon2id fixtures для проверки совместимости.
- Первый набор из 5 интеграционных сценариев — успешно: auth/roles/validation,
  students/search, verification lifecycle, QR/expiry/revoke/logs, delete/restore,
  8 параллельных сканирований. Один ALLOWED, семь DENIED, восемь записей журнала.
- TypeScript и production Vite build — успешно.
- Playwright Chromium: все семь страниц, отклонение/повторная подача/одобрение,
  генерация QR, реальное декодирование QR-пикселей html5-qrcode, повторное сканирование,
  журнал, вход TRUSTED_PERSON, logout и soft delete — успешно. API не подменяется.
  Вместо физической камеры используется синтетический видеопоток с настоящим QR.

Предоставленный backup восстановлен штатным pg_restore в отдельную UTF-8 базу:
users=6, students=8, guardian_student=6, verification_requests=10, qr_codes=9,
access_logs=8. Hibernate validate, запуск Java, SELECT 1 healthcheck и Swagger на
этой базе прошли. DDL schema.sql сравнен с pg_restore --schema-only — совпадает.
Исходная база и файлы backup не изменялись. Пароли существующих пользователей
не предоставлены, вход с их паролями не заявляется проверенным; формат Argon2id
проверен отдельным fixture.

Первый Docker CI: https://github.com/Buka2989/SecuritySystem/actions/runs/42820970072
— Maven/Testcontainers, сборка и healthchecks трёх контейнеров, Swagger и SPA
прошли. Установка Playwright остановилась из-за смены ubuntu-latest на Ubuntu 26.04.
Runner закреплён на поддерживаемой Ubuntu 24.04. Повторный CI также включает два
дополнительных интеграционных сценария: конкурентная повторная подача заявки
и откат USED при невозможности записать access_log. Итог будет записан после прогона.

Ограничения проверки: физическая камера и HTTPS на телефоне не тестировались.
Сохранённые зависимости frontend имеют предупреждения npm audit по Vite/esbuild
и react-router; исправление source-map-js внесено без major upgrade. Полное устранение
оставшихся предупреждений требует обновления major-версий frontend toolchain/router
и отдельного регрессионного прогона. Vite используется для разработки/сборки,
production обслуживает Nginx. В приложении нет SSR hydration и переходов по URL
из пользовательского ввода, но это не заменяет обновление зависимостей.
