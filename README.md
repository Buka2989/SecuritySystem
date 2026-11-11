# Security System — Java 21 / Spring Boot

A migration of [SecuritySystemv2](https://github.com/AldairRonin/SecuritySystemv2)
from FastAPI to Spring Boot, preserving the React interface and PostgreSQL schema.
The source analysis, all 21 original API operations, and documented changes are
available in [docs/MIGRATION.md](docs/MIGRATION.md) (in Russian).

This branch is the final Software Design Patterns project. It contains eight
integrated patterns, a working GUI, demo data, automated tests and submission materials.
Current verification is recorded in [docs/TESTING.md](docs/TESTING.md).
The original migration's [successful CI run](https://github.com/Buka2989/SecuritySystem/actions/runs/42821687984)
is retained as historical evidence; it does not replace this branch's checks.

## Architecture

- `backend/`: Java 21, Spring Boot 3.5, Spring Web, Data JPA, Security, Validation,
  Maven, PostgreSQL JDBC, Argon2id, JWT HS256/384/512, and Swagger/OpenAPI.
- `frontend/`: the existing React + TypeScript + Vite application for PARENT,
  TRUSTED_PERSON, SECURITY, and ADMIN users. Nginx serves the production build.
- `databases/`: the schema extracted from the supplied backup, without personal
  data, and initialization support for restoring a custom-format PostgreSQL backup.
- `docker-compose.yml`: postgres → backend:8080 → frontend:80.
  The browser sends requests to `/api/`; Nginx strips that prefix before forwarding.

## Getting started

Install Git, Docker Engine/Desktop with Linux containers, and Docker Compose v2.
Local installations of Java, Maven, Node.js, and PostgreSQL are not required for Docker.

```sh
git clone --branch design-patterns https://github.com/Buka2989/SecuritySystem.git
cd SecuritySystem
cp .env.example .env
```

In PowerShell, you can use `Copy-Item .env.example .env`.
Set `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, and `JWT_SECRET_KEY`.
Replace all example secrets. HS256 requires a key of at least 32 bytes; HS384
requires 48, and HS512 requires 64. Use randomly generated key material, for example
`openssl rand -hex 32` for HS256.
`JWT_EXPIRE_MINUTES` controls JWT lifetime. `APP_TIME_ZONE` determines how timestamps
without a timezone are interpreted. Real `.env` files and database backups are
excluded from Git; do not commit personal data.

### Starting with an existing backup

Before the **first** start, place `security_backup.backup` in `databases/import/`.
Initialization restores the data, UUIDs, relationships, and existing Argon2 password
hashes into a new database volume. The original database remains untouched.
Original ownership and GRANT statements are omitted; `POSTGRES_USER` becomes the owner.

```sh
docker compose up -d --build
docker compose ps
```

### Starting with an empty database

If no backup is present, initialization creates the six original tables without
sample data. To create the first administrator, set `BOOTSTRAP_ADMIN_USERNAME`
and `BOOTSTRAP_ADMIN_PASSWORD` (at least 12 characters), then run the same command:

```sh
docker compose up -d --build
```

The administrator is created **only when the users table is empty**. Existing
accounts and passwords are never overwritten. You can clear the bootstrap variables
after the first successful start.

By default, Compose publishes these local addresses:

- Frontend: [localhost:3002](http://localhost:3002)
- API health: [localhost:8081/health](http://localhost:8081/health)
- Swagger UI: [localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- OpenAPI JSON: [localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

If these host ports are already occupied, change only the host-side port mappings
in `docker-compose.yml` (keep the container-side ports `80` and `8080` unchanged).
If you change the frontend host port, update `CORS_ORIGINS` in `.env` to match
the frontend address you use.

In Swagger, call `POST /auth/login`, then paste the returned `access_token` into
**Authorize**. Public registration through `POST /users/` supports PARENT and
TRUSTED_PERSON accounts. An administrator uses the same endpoint to create SECURITY
and ADMIN accounts, and `POST /students/` to create students.
The original React application has no registration or user-management forms;
these operations are available through the API and Swagger. A trusted person
submits their own relationship with a student for administrator approval.

## Data storage and import

The named `postgres_data` volume survives restarts, `docker compose down`, and image
rebuilds. Initialization runs only when PGDATA is empty. Do not use
`docker compose down -v` on a database whose data you need to keep.
Replacing the backup file after the first start does not overwrite existing data.
To test another backup, use a separate Compose project with separate ports and a
separate volume.

The working schema comes from the supplied backup, not the outdated `securitysys.sql`.
Hibernate uses `ddl-auto=validate` and does not create or modify tables.
Do not directly reuse a physical PostgreSQL data volume across major versions;
use a logical backup and restore. Compose uses PostgreSQL 18 and mounts its data
volume at `/var/lib/postgresql`, as required by that image.

```sh
# Open an interactive SQL session inside the container.
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'

# Create a backup without piping binary data through PowerShell.
docker compose exec postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc -f /tmp/security_backup.backup'
docker compose cp postgres:/tmp/security_backup.backup ./security_backup.backup
```

PostgreSQL is not exposed on a host port. The backend connects to `postgres:5432`.
For development outside Docker, set `DATABASE_URL` to a JDBC URL such as
`jdbc:postgresql://127.0.0.1:5432/security_system` and configure `POSTGRES_USER`
and `POSTGRES_PASSWORD` separately. Replace SQLAlchemy URLs such as
`postgresql+psycopg://...` with JDBC URLs.

## Stopping services and troubleshooting

```sh
docker compose logs -f backend
docker compose logs -f postgres frontend
docker compose stop
docker compose start
docker compose down
```

The PostgreSQL healthcheck waits for database readiness. The backend healthcheck
runs `SELECT 1`, and the frontend starts after the backend becomes healthy.
A schema validation failure prevents backend startup: inspect the imported schema
instead of enabling automatic schema changes.

Live camera scanning works on localhost and on HTTPS. For a LAN demo over HTTP,
open the guard page on the phone and choose **Сфотографировать QR**. Take a photo
of the QR on the computer screen, or select an existing photo if the browser
opens a file picker. The browser decodes the image locally and sends the QR token
to the verification endpoint.

Parents and trusted persons see only students they can add in the search results.
Review banners are shown once per browser and account. Administrators can enter
a rejection reason directly in the request card.

## Development and testing

Install JDK 21, Maven 3.9+, Node.js 22.12+ with npm, and Docker for Testcontainers.

```sh
cd backend
mvn test
mvn verify
cd ../frontend
npm ci
npm run build
npm run dev
```

`mvn test` runs JWT, QR policy, Python-format compatibility and design-pattern unit tests.
`mvn verify` also runs `ApiIT` against a real PostgreSQL 18 instance through
Testcontainers. Coverage includes authentication, roles, validation, student search,
relationships, verification approval/rejection/resubmission, soft deletion and
restoration, QR expiry and revocation, repeated and concurrent scans, audit logs,
concurrent resubmission, and transaction rollback when an audit insert fails.
Integration tests are not silently skipped when Docker is unavailable.

Alternatively, configure `TEST_DATABASE_URL`, `TEST_DATABASE_USER`, and
`TEST_DATABASE_PASSWORD` for a dedicated local PostgreSQL test database.
The URL must end with `/security_test`, and the database must be new and empty.
Tests initialize its schema and clear tables between scenarios. Never point these
variables at a working database.

GitHub Actions builds and starts all three Docker services, runs API and browser
tests, checks persistence after container recreation, and uploads reports.
Detailed results and test limitations are recorded in
[docs/TESTING.md](docs/TESTING.md) (in Russian).

## Known limitations inherited from the original model

Unknown QR tokens cannot be inserted into `access_logs` without changing its
mandatory foreign key. Those attempts are recorded in backend application logs;
scans of existing QR codes are recorded in PostgreSQL.
Timestamps remain timezone-free for compatibility, so `APP_TIME_ZONE` must match
the timezone used by the original Python process.
There is no facial recognition or integration with government passport systems.

## Design patterns branch

The `design-patterns` branch extends the application with student editing,
selectable contains/prefix search, review notifications, configurable QR lifetime,
and access report downloads (CSV or JSON, optionally redacted). Existing API
defaults and the PostgreSQL schema are preserved.

| Pattern | Implementation and application role |
|---|---|
| Builder | `QrIssuePlan` validates and constructs a QR issuance plan |
| Factory Method | `ReportExporter` subclasses create CSV/JSON documents |
| Adapter | `AccessLogReportAdapter` converts persisted records into report entries |
| Decorator | `RedactedReportSource` hides identifiers and names in exports |
| Facade | `AccessReportFacade` coordinates report filtering, statistics and export |
| Strategy | `ContainsSearch` and `PrefixSearch` implement user-selected search |
| Observer | `ReviewPublisher` notifies inbox and audit subscribers after commit |
| Visitor | `StatisticsVisitor` aggregates allowed and denied report entries |

### Four-person pattern allocation

The course requires each student to independently implement and explain at least
two patterns. The following allocation assigns all eight patterns across four
students. Replace the member labels with the students' actual names before submission
and make sure the allocation reflects who actually implemented each pattern.

| Team member | Assigned patterns | Main implementation |
|---|---|---|
| Member 1 | Factory Method, Strategy | `ReportExporter`, `ContainsSearch`, `PrefixSearch` |
| Member 2 | Builder, Visitor | `QrIssuePlan`, `ReportVisitor`, `StatisticsVisitor` |
| Member 3 | Adapter, Decorator | `AccessLogReportAdapter`, `RedactedReportSource` |
| Member 4 | Facade, Observer | `AccessReportFacade`, `ReviewPublisher`, subscribers |

Review notifications are transient and bounded; persisted verification decisions
remain the source of truth. A browser polls its own authenticated inbox and refreshes
relationship status when a decision arrives. Reports are restricted to administrators
and security staff; student changes require an administrator.

### Demonstration data

Use a separate, empty database volume for demonstrations. Set
`SPRING_PROFILES_ACTIVE=demo` and `DEMO_PASSWORD` (at least 12 characters) in `.env`,
then run `docker compose up -d --build`. The demo profile seeds `demo_admin`,
`demo_parent`, `demo_guard`, and `demo_trusted` with that password, two students,
pending/approved relationships, and allowed/denied access records. Seeding is skipped
when users or students already exist. Never delete a working volume to enable demo mode.

As administrator, create/edit a student. As parent, select a search mode, request a
relationship, and wait for an administrator decision. The notification updates the
dashboard; open the student, choose the QR lifetime, and generate a code. A guard
can scan it once and inspect history. As guard or administrator, open the access
report, choose dates, format and redaction, then download the result.

Course requirements and outstanding team responsibilities are tracked in
[the final project plan](docs/FINAL_PROJECT_PLAN.md). The confirmed allocation gives
each of the four students two patterns. See the [requirements audit](docs/REQUIREMENTS_AUDIT.md)
and [individual defense guide](docs/DEFENSE.md) for implementation evidence,
demonstrations, tradeoffs and extension exercises.

### Final submission materials

- [English PDF report](output/pdf/security-system-report.pdf): scenarios, architecture,
  eight-pattern table, UML, code excerpts, screenshots, test results and responsibilities.
- [Editable UML](docs/uml): Mermaid sources and rendered SVG/PDF figures.
- [Test results](docs/TESTING.md): inputs, expectations, actual outcomes and limitations.
- `output/security-system-submission.zip`: source archive generated locally for Moodle.
  It includes the report and excludes real `.env`, backups, dependencies and build caches.

To regenerate the report and source archive, install Python 3.11+ with ReportLab and
Pillow, then run from the repository root:

```sh
python -m pip install reportlab Pillow
python scripts/build_report.py
python scripts/package_submission.py
```

To run browser checks against an **isolated demo deployment**:

```sh
cd e2e
npm ci
npx playwright install chromium
# Set E2E_ALLOW_WRITES=1, E2E_ADMIN_USERNAME=demo_admin,
# and E2E_ADMIN_PASSWORD to your configured DEMO_PASSWORD.
npm test
```

In PowerShell use `$env:E2E_ALLOW_WRITES='1'` and the same syntax for the other
variables. Set `CAPTURE_REPORT=1` to refresh the five screenshots during the tests.
Tests create synthetic accounts and pupils, so use a separate demonstration database.
The UI is in Russian; the report and setup documentation are in English.

AI assistance from OpenAI Codex was used for analysis, implementation support,
tests and documentation. The report discloses this assistance. Students must
understand, test and modify their allocated code and complete their own defense.
