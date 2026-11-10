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

The team will provide the allocation. No independent implementation is attributed
to a student until actual contributions have been confirmed.

| Student | Patterns | Application responsibility |
|---|---|---|
| Aldiyar Adilkhan | Awaiting team allocation | Awaiting team allocation |
| Baktiyar Kaiyrzhan | Awaiting team allocation | Awaiting team allocation |
| Bauyrzhan Muratkhanov | Awaiting team allocation | Awaiting team allocation |
| Zhalgas Sarsenov | Awaiting team allocation | Awaiting team allocation |

Patterns must appear in executed application paths, with tests and demonstration
steps. Framework implementations alone are not counted.

## Submission conditions requiring team participation

The supplied assignment requires independent implementation, an individual defense,
weekly progress presentations, and disclosure of tools and external materials.
The repository can supply working features, documentation, and preparation exercises;
it cannot establish that each student independently wrote code or attended presentations.
Actual contributions and the final disclosure section must be confirmed by the team.
