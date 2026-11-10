# Individual defense guide

## Demonstrate the complete journey

1. Start the demo profile as described in README. Sign in as `demo_admin`.
2. Open Students. Create a pupil, edit the class, then leave the pupil active.
3. In a separate browser session, sign in as `demo_parent`. Find that pupil,
   switching between contains and prefix search. Submit a relationship request.
4. Approve the request as administrator. The parent's notification and dashboard
   must update without manually reloading the page.
5. Open the pupil as parent, select five minutes and generate a QR.
6. Sign in as `demo_guard`. Scan the QR. The first attempt is allowed and the
   second denied. Both attempts appear in the persisted access history.
7. Open Access Report. Check totals, filter dates and export CSV and JSON.
   Toggle redaction and compare exported names and identifiers.
8. Restart the deployment and confirm the pupil, relationship and access history
   remain. Transient notifications are intentionally not persisted.

## Aldiyar Adilkhan

**Factory Method.** Open `ReportExporter`, `CsvReportExporter`, `JsonReportExporter`
and `ReportDocument`. `export` is the common workflow. Each subclass overrides
`createDocument` to supply a different product. Demonstrate both downloaded formats.
The factory adds useful extension points, but requires several small classes.
Exercise: add a plain-text document and exporter, then add its option in the UI.

**Strategy.** Open `StudentSearchStrategy`, `ContainsSearch`, `PrefixSearch` and
`StudentService.search`. The UI selects the strategy key; the service delegates
without an algorithm-specific switch. Search `Child` against `Test Child`:
contains matches; prefix does not. Exercise: add an exact-name search strategy.
Explain that PostgreSQL ILIKE currently treats `%` and `_` as search wildcards.

## Baktiyar Kaiyrzhan

**Builder.** Open `QrIssuePlan` and `QrService.create`. The builder collects the
relationship, issue time and optional lifetime, checks its invariants, and builds
an immutable plan. The plan issues a new QR using a cryptographically random token.
Demonstrate five-minute expiry and explain the default thirty-minute lifetime.
Exercise: add a named builder method for a short-lived two-minute pass.

**Observer.** Open `ReviewPublisher`, `ReviewSubscriber`, `ReviewInbox`,
`ReviewAuditSubscriber` and `VerificationService.review`. The application's own
publisher dispatches to subscribers after database commit. A rollback must never
notify the guardian. The browser polls its private inbox every three seconds.
Exercise: add a subscriber counting approved reviews; confirm subscriber failure
does not prevent delivery to the others. Explain that notifications are bounded,
transient and local to one backend process; a durable broker would be needed at scale.

## Bauyrzhan Muratkhanov

**Adapter.** Open `ReportSource` and `AccessLogReportAdapter`. The adapter reads
the existing six-table model and exposes typed allowed/denied report entries.
No reporting-specific schema is required. Demonstrate a scanned QR in the export.
Exercise: adapt an in-memory fixture source for a test. Explain the current
per-record repository lookups and how a joined projection could improve performance.

**Decorator.** Open `RedactedReportSource`. It implements the same interface as
its wrapped source and transforms a copy of its entries. Toggle redaction in the
interface; names become Hidden and record IDs become empty while totals remain.
Exercise: add a decorator that includes denied entries only. Explain that this
redaction hides selected fields, but timestamps and reasons remain in the report.

## Zhalgas Sarsenov

**Facade.** Open `AccessReportFacade` and `ReportController`. The controller asks
the facade for a summary or export. The facade validates dates and coordinates
adaptation, filtering, optional decoration, visiting and document creation.
Exercise: add a convenience operation for today's summary without duplicating
validation. Explain why authorization remains at the controller boundary.

**Visitor.** Open `ReportVisitor`, `ReportEntry` and `StatisticsVisitor`.
Each entry accepts the visitor and selects the appropriate overloaded visit method.
The visitor accumulates allowed/denied counts and denial reasons. Demonstrate those
counts after first and repeated scans. Exercise: add another visitor calculating
counts per day. Adding an entry subtype requires updating every visitor.

## Questions every member should answer

- How does a browser action reach a controller, service, repository and PostgreSQL?
- Why are interface logic, business decisions and storage kept separate?
- How do row locks prevent two guards from consuming the same QR?
- Why must the access-log insert and QR consumption share one transaction?
- What happens when an API call fails or a user lacks a required role?
- Which changes require schema changes, and which only add a pattern participant?
- How do you run unit, integration and browser tests from a clean clone?

AI assistance from OpenAI Codex was used for analysis, implementation support,
tests and documentation. Each member must study, run and modify their allocated
parts and comply with the course's independent-work requirements.
