# AdSphere

A full-stack advertising campaign platform: plan campaigns, define placements, browse audience
segments, and bid on publisher inventory.

| Layer    | Technology                                                             |
| -------- | ---------------------------------------------------------------------- |
| Frontend | React 18.3.1, React Router 6, Vite 5                                   |
| Backend  | Java 11, Spring Boot 2.1.0.RELEASE (Web, Data JPA, Security, Validation) |
| Auth     | Stateless JWT (HS256), BCrypt passwords, role-based access            |
| Storage  | H2 (in-memory by default; configurable via JDBC URL)                  |
| Build    | Bazel 6.4.0 running on a hermetic JDK 21 (bytecode targets Java 11)   |

## Features

- **Campaigns**: paged list (5 per page) with header filters for status, date range, objective and
  name. Also row actions (view/edit/delete) and bulk delete. **Create Campaign** and **Create
  Placement** each open their own page.
- **Create Campaign form**: name, objective, start/end dates, budget, status and description. Under
  *Assign Placements* you can select existing placements (searchable multi-select) or create a
  new placement inline, saved in the same transaction.
- **Create Placement form**: country, audience group, video targeting, traffic (App/Website), ad
  position, deal type, frequency cap, device targeting, ad format and notes.
- **Audience**: segment list with search and type/status filters. Admins can activate or deactivate
  a segment.
- **Inventory**: deal list with search and filters. Place a CPM bid against a campaign. A bid at or
  above the floor price wins right away: the deal is marked **Sold** and that campaign's ads serve
  on it. A bid below the floor is recorded as rejected.
- **Settings**: General, Integrations, Notifications, Security (admins edit; others read-only).
- **Help**: documentation, searchable FAQs, and a contact-support form (stores a ticket).
- **Profile**: account details, edit profile, change password, login activity, sign out.
- Validation on both sides:
  - Client-side on blur and on submit.
  - Server-side through Bean Validation, with field errors mapped back to the inputs.
- Responsive layout: the sidebar becomes a drawer and tables turn into cards on small screens.

### Roles

| Role      | Can do                                                                  |
| --------- | ----------------------------------------------------------------------- |
| `ADMIN`   | Everything, including settings, deleting campaigns, audience status     |
| `MANAGER` | Create/edit campaigns and placements, bid on inventory                  |
| `VIEWER`  | Read-only                                                               |

Rules are enforced in the Spring Security filter chain and again with `@PreAuthorize`. The UI
hides actions the current role cannot perform.

Demo accounts (seeded into an empty database): `admin / Admin@123`, `manager / Manager@123`,
`viewer / Viewer@123`.

## Repository layout

```
WORKSPACE, .bazelrc, .bazelversion   Bazel 6.4.0 setup, Maven deps, JDK 21 toolchains
backend/                             Spring Boot service
  src/main/java/com/adsphere/
    config/       security, seed data, SPA forwarding
    domain/       JPA entities and enums
    dto/          request/response payloads
    repository/   Spring Data repositories + specification helpers
    security/     JWT issuing/validation, filter, JSON 401/403
    service/      business logic (campaigns, placements, bidding, ...)
    validation/   custom constraints (date range)
    web/          REST controllers, exception handling
  src/test/java/  MockMvc API tests + unit tests
frontend/                            React app (Vite)
  src/api/        fetch client and endpoint wrappers
  src/auth/       auth context, route guard, roles
  src/components/ shared UI (layout, tables, forms, modal, ...)
  src/pages/      feature pages
tools/springfactories/               Bazel helper, see "Notes"
deploy/                              Dockerfile and environment template
```

## Prerequisites

- **Bazel 6.4.0**: install [Bazelisk](https://github.com/bazelbuild/bazelisk). It reads
  `.bazelversion`. A local JDK is not required; Bazel downloads JDK 21.
- **Node.js 18+** and npm, for the frontend.

## Running locally

```bash
# 1. API on http://localhost:8080
bazel run //backend:app

# 2. UI on http://localhost:5173 (proxies /api to :8080)
cd frontend
npm install
npm run dev
```

To proxy to a backend elsewhere, set `VITE_BACKEND_URL=http://host:port npm run dev`.

## Tests

```bash
bazel test //backend/...          # Spring Boot API + unit tests (JDK 21)
cd frontend && npm test           # Vitest: validators and form hook
```

## Building for deployment

```bash
# API-only fat jar
bazel build //backend:server_deploy.jar

# Single jar with the UI bundled (runs `npm ci` + `vite build` via Bazel; needs node on PATH)
bazel build --config=ui //backend:server_with_ui_deploy.jar
java --add-opens=java.base/java.lang=ALL-UNNAMED -jar bazel-bin/backend/server_with_ui_deploy.jar
```

Docker:

```bash
bazel build --config=ui //backend:server_with_ui_deploy.jar
cp -f bazel-bin/backend/server_with_ui_deploy.jar deploy/app.jar
docker build -t adsphere deploy/
docker run -p 8080:8080 --env-file deploy/.env adsphere   # see deploy/.env.example
```

### Configuration

| Variable                     | Default                         | Purpose                               |
| ---------------------------- | ------------------------------- | ------------------------------------- |
| `PORT`                       | `8080`                          | HTTP port                             |
| `APP_JWT_SECRET`             | dev-only value                  | **Set in every real environment** (≥ 32 chars) |
| `APP_JWT_EXPIRATION_MINUTES` | `60`                            | Token lifetime                        |
| `APP_CORS_ALLOWED_ORIGINS`   | `http://localhost:5173`         | Comma-separated origins for cross-origin UI hosting |
| `APP_SEED_ENABLED`           | `true`                          | Seed demo data into an empty database |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | in-memory H2        | JDBC connection                       |
| `DDL_AUTO`                   | `update`                        | Hibernate schema mode                 |

## REST API

All endpoints except `POST /api/auth/login` and `GET /actuator/health` require
`Authorization: Bearer <token>`. Errors share one JSON shape:
`{ status, error, message, path, fieldErrors }`.

| Method | Path                         | Notes                                                       |
| ------ | ---------------------------- | ----------------------------------------------------------- |
| POST   | `/api/auth/login`            | `{ username, password }` → `{ token, expiresAt, user }`     |
| GET    | `/api/auth/me`               | Current user                                                |
| GET    | `/api/campaigns`             | `status, objective, name, from, to, page, size, sort`       |
| GET    | `/api/campaigns/{id}`        |                                                             |
| POST   | `/api/campaigns`             | ADMIN/MANAGER; `placementIds[]` and/or `newPlacement{}`     |
| PUT    | `/api/campaigns/{id}`        | ADMIN/MANAGER                                               |
| DELETE | `/api/campaigns/{id}`        | ADMIN                                                       |
| GET    | `/api/placements`            | `q, page, size`                                             |
| POST   | `/api/placements`            | ADMIN/MANAGER                                               |
| GET    | `/api/audiences`             | `q, type, status, page, size`                               |
| PATCH  | `/api/audiences/{id}/status` | ADMIN; `{ status }`                                         |
| GET    | `/api/deals`                 | `q, type, status, page, size`                               |
| POST   | `/api/deals/{id}/bids`       | ADMIN/MANAGER; `{ amount, campaignId }`                     |
| GET/PUT| `/api/settings`              | PUT is ADMIN only                                           |
| GET/PUT| `/api/profile`               | Own profile                                                 |
| PUT    | `/api/profile/password`      | `{ currentPassword, newPassword }`                          |
| GET    | `/api/help/faqs`             |                                                             |
| POST   | `/api/help/tickets`          | `{ subject, message }`                                      |
| GET    | `/api/lookups`               | Enum values and labels used by the UI                       |

## Notes

- **JDK 21 with Bazel 6.4**: Bazel 6.4 only bundles JDKs up to 17, so `WORKSPACE` registers
  Zulu JDK 21 archives for Linux and macOS (x86_64 and arm64). `.bazelrc` selects them with
  `--java_runtime_version=remotejdk_21` and `--tool_java_runtime_version=remotejdk_21`.
  Sources compile with `--java_language_version=11`.
- **Spring Boot 2.1 on JDK 21**:
  - Byte Buddy is pinned to 1.14.9, since the 2018 release doesn't understand newer class files.
  - The JVM runs with `--add-opens java.base/java.lang` for CGLIB.
- **`spring.factories` merging**: Bazel's deploy jar keeps only the first copy of duplicate
  resources, which would drop most Spring Boot auto-configurations.
  `//tools/springfactories:merger` merges every `META-INF/spring.factories` on the classpath into
  one file, and that file is placed first in the deploy jars.
