# Проверки

Дата: 10 ноября 2026. Итоговый прогон — **SUCCESS**:
[GitHub Actions #2](https://github.com/Buka2989/SecuritySystem/actions/runs/42821687984),
код commit `480e165abde0d614ea883dd2184934c0b875ea38`.

В CI подтверждены:

- 14 unit-тестов и 7 интеграционных тестов на PostgreSQL 18/Testcontainers:
  0 failures, 0 errors, 0 skipped.
- Сборка Java 21 JAR и React production bundle в многоэтапных Dockerfile.
- Запуск postgres, backend, frontend; все три healthcheck успешны.
- HTTP health, OpenAPI, Swagger и Nginx SPA routes.
- Playwright Chromium workflow всех страниц, включая реальное декодирование QR,
  отказ при повторном сканировании и отображение журнала.
- Сохранение access_logs после docker compose down и последующего up.
- Конкурентная повторная подача создаёт ровно одну PENDING-заявку.
- Ошибка записи access_log откатывает изменение QR на USED. Ошибка CHECK
  test_reject_allowed в CI-логе ожидаема: её намеренно вызывает этот тест.

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
Runner закреплён на поддерживаемой Ubuntu 24.04; повторный прогон полностью успешен.
Docker проверен в GitHub Actions, поскольку на исходном Windows-компьютере Docker
не установлен. Восстановление предоставленного персонального backup выполнено только
локально; в публичный GitHub и CI он не отправлялся.

Ограничения проверки: физическая камера и HTTPS на телефоне не тестировались.
Сохранённые зависимости frontend имеют предупреждения npm audit по Vite/esbuild
и react-router; исправление source-map-js внесено без major upgrade. Полное устранение
оставшихся предупреждений требует обновления major-версий frontend toolchain/router
и отдельного регрессионного прогона. Vite используется для разработки/сборки,
production обслуживает Nginx. В приложении нет SSR hydration и переходов по URL
из пользовательского ввода, но это не заменяет обновление зависимостей.
