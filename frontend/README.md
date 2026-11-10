# React frontend

Исходный интерфейс SecuritySystemv2, сохранён при переносе backend на Java.
Семь страниц обслуживают родителей, доверенных лиц, администратора и охранника.
Дизайн и CSS не менялись. Реальные API-запросы через services/api; mock-сервиса нет.

`npm ci && npm run build` — production build.
`npm run dev` — Vite на localhost:5173 с proxy /api → localhost:8080.
Docker и настройка БД описаны в корневом README.
