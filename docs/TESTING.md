# Verification of the final project

Verified on 2026-11-11, branch `design-patterns`. This records the current final-project
checks; the original migration CI run linked in README is historical evidence.

## Results

- Maven verify: **24 unit tests + 11 PostgreSQL integration tests**,
  zero failures, errors or skipped tests. PostgreSQL 18 runs through Testcontainers.
- Playwright Chromium: **2 complete scenarios passed**, zero unexpected,
  flaky or skipped tests. The tests use the real API and database.
- Frontend TypeScript and Vite 8 production build passed. npm audit: **0 vulnerabilities**.
- Docker Compose: PostgreSQL, Java 21 backend and Nginx frontend are healthy.
- All six table counts and complete row checksums match after container recreation.

## Inputs, expectations and actual outcomes

| Input / scenario | Expected result | Actual result |
|---|---|---|
| Main journey: request, reject/resubmit, approve, issue QR, scan twice. | First scan ALLOWED; repeated scan DENIED; both saved. | PASS - workflow.spec.js; actual QR pixels decoded. |
| Search Test Child with Child in contains/prefix modes; edit class. | Contains returns one; prefix zero; edited class reloads. | PASS - ApiIT and patterns.spec.js. |
| Commit approval; roll back a queued review; read another inbox. | Only owner receives committed notice; rollback is silent. | PASS - Observer unit/API tests and live UI notification. |
| Recreate all Compose containers without removing volume. | All six tables retain row counts and full content. | PASS - six table checksums match. |
| Export two or more saved scans as CSV/JSON; toggle redaction. | Valid documents; hidden names/IDs; original data unchanged. | PASS - API, factory/decorator tests and browser downloads. |
| Issue QR for 5 minutes; try 0 and 1,441 minutes. | Five-minute interval; invalid lifetimes rejected with 422. | PASS - Builder tests and ApiIT. |
| Blank student name/class; deactivate a linked student. | Blank data rejected with 422; issue/scan disallowed when inactive. | PASS - student editing and inactive-pupil API tests. |
| Unknown search mode, XML export, reversed date range. | 422 and readable errors; inclusive same-day filters work. | PASS - Facade/API tests and browser error assertions. |
| Parent requests staff reports; another guardian revokes QR. | 403 for staff resource; foreign QR is not exposed (404). | PASS - role/ownership API tests. |
| Eight concurrent scans; four simultaneous resubmissions. | Exactly one ALLOWED and one new pending request. | PASS - concurrent ApiIT scenarios. |
| Force final access-log insert to violate a database constraint. | 409; consumption rolled back to ACTIVE; next scan succeeds. | PASS - transaction rollback ApiIT. |

## Reproduce

Run `mvn -B -f backend/pom.xml verify` using JDK 21 and Maven 3.9+ with Docker.
The Java sources compile with release 21; the local Windows verification additionally
passed using OpenJDK 25.0.1. Tests use a disposable database named security_test;
they never target the demo or working database.

The test resource selects Mockito's subclass mock maker. Spring's test listeners
inspect beans even when the application uses no mocked services. This setting avoids
a fatal inline-agent attachment problem on Windows with a Cyrillic user directory.
On that host Spring's optional inline-maker probe can still print an agent warning;
the actual tests pass without skipping or substituting business operations.

Run `npm ci`, `npm run build` and `npm audit` in frontend using Node 22.12+.
Run `npm ci`, `npx playwright install chromium` and `npm test` in e2e. Set
E2E_ALLOW_WRITES=1 and administrator credentials for an isolated deployment as
documented in README. Set CAPTURE_REPORT=1 to refresh the report screenshots.

Run `python scripts/check_persistence.py --project YOUR_ISOLATED_PROJECT` after
the demonstration. This performs compose down/up without deleting volumes and
compares counts and hashes of all rows, ordered by UUID, for all six tables.
`docs/persistence-result.json` records the resulting counts and digests.

Maven XML reports are under backend/target/surefire-reports and failsafe-reports.
Playwright writes HTML under e2e/playwright-report and machine-readable JSON under
e2e/test-results/results.json. CI uploads reports and container logs. Local traces,
credentials, dependency caches and build outputs are excluded from the source ZIP.

## Coverage and limits

Unit checks cover JWT signatures/expiry, inherited Python JWT/Argon2 compatibility,
QR rejection rules, Builder invariants, document factories, CSV escaping/formula
neutralization, redaction immutability, Visitor statistics, Facade date boundaries
and Observer commit/rollback/error isolation. API checks also cover database locks,
ownership, validation, soft deletion/restoration, inactive students and transactional
rollback. The deliberately triggered test_reject_allowed CHECK error is expected.

The browser workflow decodes real QR pixels through html5-qrcode using a synthetic
camera stream. A physical camera and phone HTTPS deployment were not tested.
Notifications are bounded and transient; persisted verification decisions remain the
source of truth. Search currently supports PostgreSQL ILIKE wildcards. Reports use
per-record lookups suitable for demonstration data; pagination/joined projections
are future work. CSV redaction hides names and IDs while retaining reasons/times.
The production bundle has a size warning; it builds successfully and is served by Nginx.
