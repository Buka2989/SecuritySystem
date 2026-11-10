# Final project implementation plan

## Scope

Extend the existing school access application on `design-patterns`, preserving
the PostgreSQL schema, original API contracts, JWT authorization, and atomic QR use.
The complete journey is: select a student, configure a relationship and QR lifetime,
submit and review verification, scan the QR, inspect the saved result, and export
an access report.

## Requirements and implementation targets

| Requirement | Implementation target |
|---|---|
| Three related entities | Existing six PostgreSQL entities |
| Five scenarios | Student administration, linking, review, QR issue, scan, reporting |
| Create, view, edit, cancel | Student editor, existing links and cancellation, QR revocation |
| Two selectable behaviors | Contains and prefix student search strategies |
| Event reaction | Custom domain event publisher and subscribers; guardian notifications after review |
| Persistence | Existing PostgreSQL tables and Docker volume |
| Save or export output | Authorized access report with CSV/JSON export and redaction option |
| Sample data | Explicit demo initialization for an empty database |
| Eight or more test scenarios | Automated unit/API/browser tests with inputs, expectations, actual results |
| Submission | English README, PDF report, UML source/renderings, screenshots, source ZIP |

## Responsibility allocation

The team confirmed this responsibility allocation. Each student owns two patterns,
their tests, demonstration and explanation at the individual defense. This allocation
does not assert that AI-assisted code was independently authored by a student.

| Student | Patterns | Application responsibility |
|---|---|---|
| Aldiyar Adilkhan | Factory Method, Strategy | CSV/JSON document creation and selectable student search |
| Baktiyar Kaiyrzhan | Builder, Observer | QR configuration and review event delivery |
| Bauyrzhan Muratkhanov | Decorator, Adapter | Report privacy and mapping persisted access records |
| Zhalgas Sarsenov | Facade, Visitor | Report orchestration and access statistics |

Patterns must appear in executed application paths, with tests and demonstration
steps. Framework implementations alone are not counted.

## Submission conditions requiring team participation

The supplied assignment requires independent implementation, an individual defense,
weekly progress presentations, and disclosure of tools and external materials.
The repository can supply working features, documentation, and preparation exercises;
it cannot establish that each student independently wrote code or attended presentations.
The report discloses Codex assistance. Each member must verify their own contribution,
understand and modify their allocated patterns, and complete the individual defense.
