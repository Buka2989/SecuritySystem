# Final project requirements audit

Reference: the supplied FINAL PROJECT assignment. Working branch: `design-patterns`.

| Requirement | Evidence |
|---|---|
| Java or Go core | Java services and application-owned patterns under `backend/src/main/java` |
| Graphical interface connected to logic | React forms call authenticated Spring REST controllers; Playwright uses real API and database |
| Three related entities | User, Student, GuardianStudent, VerificationRequest, QrCode, AccessLog |
| Five user scenarios | Student administration, search, relationship request, review, QR issue/revoke, scan, reporting |
| Create, view, edit, cancel | Student creation/editing/inactivation; request resubmission; relationship soft deletion; QR revocation |
| Two interchangeable algorithms | ContainsSearch / PrefixSearch selected in the search form |
| Event reaction | ReviewPublisher dispatches after commit to inbox and audit subscribers; UI refreshes on notification |
| Persistent storage | PostgreSQL plus named Docker volume; restart verification documented in TESTING |
| Save final output | Authorized CSV/JSON report downloads with date filters and optional redaction |
| Validation and error handling | Jakarta constraints, domain rules, HTTP 401/403/404/409/422, readable UI errors |
| Immediate sample data | Explicit demo profile seeds all four roles, two students, relationships and scan records |
| Six integrated patterns and all categories | Eight: Builder, Factory Method, Adapter, Decorator, Facade, Strategy, Observer, Visitor |
| Two patterns per student | Team-confirmed responsibility table in FINAL_PROJECT_PLAN and report |
| Separated layers | Controllers/DTOs, services/patterns, JPA repositories/entities, React UI |
| Eight scenarios and three automated logic tests | Unit, PostgreSQL API and browser suites; scenario matrix with actual outcomes in TESTING |
| README | Environment, branch, setup, demo credentials configuration, testing and persistence commands |
| PDF report | `output/pdf/security-system-report.pdf`; generated from `scripts/build_report.py` |
| Class and sequence UML | `docs/uml` sources and rendered figures, embedded in PDF |
| Screenshots and code excerpts | Included in PDF; screenshots under `docs/screenshots` |
| Source ZIP and repository link | `output/security-system-submission.zip`; archive script excludes credentials and build caches |
| Individual defense | Allocated demonstrations, tradeoffs and modification exercises in DEFENSE |
| AI disclosure | Report and DEFENSE describe Codex assistance |

Weekly presentations, actual independent implementation and individual oral defense
are performed by the students. Repository artifacts cannot establish attendance or
replace those activities. The responsibility allocation identifies who prepares,
validates and explains each part; it does not manufacture a development history.
