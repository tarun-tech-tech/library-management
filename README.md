# SmartLib

SmartLib is a production-oriented library management system being rebuilt from the original NetBeans Ant application into a secure Spring Boot service with a premium Thymeleaf workspace. The legacy application remains in place for reference and data migration work; the new application is the root Maven project.

## Current result

The current workspace contains:

- A Spring Boot 3.4 / Java 21 application under `src/main/java/com/smartlib`.
- PostgreSQL persistence with Flyway migration `V1__initial_schema.sql`.
- BCrypt authentication, database-backed sessions, CSRF protection, role checks, and environment-only admin bootstrap.
- Catalog, physical-copy, member, loan, return, renewal, fine, and deterministic reading-signal API boundaries. No external AI provider is required.
- Transactional circulation with pessimistic copy/book locks and decimal-safe fine calculation.
- Responsive Thymeleaf pages for sign-in, dashboard, catalog, book detail, circulation, and reading signals.
- Fine ledger page for member balances and authorized staff payment recording.
- A Vercel-compatible static visual preview under `public/`. The preview uses clearly labelled sample values; the Spring Boot pages use database-backed values.
- Docker and Docker Compose configuration for the Spring Boot service and PostgreSQL.
- A running design and implementation record in `SMARTLIB_DESIGN_AND_STATUS.md`.

The system is not being described as complete yet. Email delivery, report exports, barcode/QR workflows, password reset, reservations, audit logs, and broader integration/security tests remain follow-up work documented in the status file.

## Stack

- Java 21, pinned in `.java-version` and the Maven compiler configuration.
- Spring Boot 3.4.5, Spring MVC, Thymeleaf, Spring Data JPA, Spring Security, Spring Session JDBC, and Actuator.
- PostgreSQL 16 for the new service. The legacy application uses MySQL and remains separate.
- Flyway for schema ownership. Hibernate is configured with `ddl-auto: validate`.
- Maven Wrapper 3.9.11 in `mvnw` and `mvnw.cmd`.
- Outfit typography, dark navy surfaces, acid-lime signal color, editorial spacing, real image context, and GSAP/ScrollTrigger motion on the web workspace.

## Project map

```text
src/main/java/com/smartlib/          Spring Boot domain, API, security, and services
src/main/resources/templates/       Thymeleaf workspace pages and fragments
src/main/resources/static/           SmartLib CSS and JavaScript
src/main/resources/db/migration/     Flyway migrations
public/                              Vercel static preview
Dockerfile                            Reproducible Java container build
compose.yaml                          Local PostgreSQL and application services
SMARTLIB_DESIGN_AND_STATUS.md        Design rationale, decisions, checks, and gaps
src/java, web, database/              Preserved legacy Ant/GlassFish/MySQL app
```

## Local setup

### Windows PowerShell

Prerequisites: Java 21, Docker Desktop for PostgreSQL, and network access the first time the Maven wrapper downloads Maven.

```powershell
Copy-Item .env.example .env
# Edit .env and replace every placeholder with private values.
```

The service is available at `http://localhost:8080`. Open `/login`, then use the bootstrap values supplied through `.env`. The bootstrap account is created only when all four `SMARTLIB_BOOTSTRAP_ADMIN_*` variables are present and the password is at least 16 characters. After the first successful bootstrap, the values may be removed; Compose treats them as optional and the application simply disables bootstrap when they are absent. There are no committed credentials.

Run the checks without Docker:

```powershell
.\mvnw.cmd -B -ntp test
.\mvnw.cmd -B -ntp package
```

Run the service against a separately managed PostgreSQL instance:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/smartlib"
$env:DB_USERNAME = "smartlib"
$env:DB_PASSWORD = "replace-with-a-private-value"
.\mvnw.cmd spring-boot:run
```

### Linux or macOS

```bash
cp .env.example .env
chmod +x mvnw
docker compose up --build
./mvnw -B -ntp test
./mvnw -B -ntp package
```

Flyway runs automatically at application startup and creates the schema from `src/main/resources/db/migration`.

For a production container, set `SPRING_PROFILES_ACTIVE=prod` and `SESSION_COOKIE_SECURE=true` behind HTTPS. The production profile enables secure session cookies; do not expose the application over plain HTTP in production.

## API surface

The current API is session-authenticated unless noted otherwise:

- `GET /api/v1/auth/csrf`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`
- `GET /api/v1/books` and `GET /api/v1/books/{id}`
- `GET /api/v1/books/{id}/copies`
- `POST /api/v1/books` and `PUT /api/v1/books/{id}` for administrators and librarians
- `GET /api/v1/loans`
- `POST /api/v1/loans`, `POST /api/v1/loans/{id}/return`, and `POST /api/v1/loans/{id}/renew`
- `GET /api/v1/recommendations` returns deterministic catalog matches based on availability and optional member history; it does not call an external AI provider.
- `GET /api/v1/fines` and `POST /api/v1/fines/{id}/payments` record authorized, decimal-safe offline payments with method and optional reference. No payment gateway success is simulated.
- Member endpoints under `/api/v1/members`
- `GET /actuator/health`

OpenAPI UI is available at `/swagger-ui.html` when the service is running. The generated document is available at `/v3/api-docs`.

Browser clients should request `/api/v1/auth/csrf` before a state-changing request and send the returned token as `X-XSRF-TOKEN`.

## Vercel preview

Vercel cannot run the Spring Boot Java container as a native serverless runtime. This project is therefore Vercel-ready as a static, frontend-only visual preview while the real application runs from the Docker container.

The preview is in `public/` and is routed by `vercel.json`:

```bash
npx vercel@41.4.0 login
npx vercel@41.4.0 --prod
```

The preview deliberately labels its figures as illustrative. It demonstrates the information architecture, responsive behavior, image direction, accordion/carousel interactions, and visual system; it does not pretend to perform real circulation mutations. The functional Java backend is deployed separately to a Java container platform. Do not place private API credentials or database URLs in the Vercel static project.

## Recommended deployment path

Deploy the two parts separately:

### 1. Deploy the functional backend on Render

Render is the simplest first Java-container deployment for this repository.

1. Create a **PostgreSQL** database in Render.
2. Create a **Blueprint** from `tarun-tech-tech/library-management`. Render will detect `render.yaml` and create the Docker web service configuration.
3. Select the `main` branch. The blueprint uses the repository `Dockerfile` and `/actuator/health` as its health check.
4. Add the managed database connection and bootstrap values requested by the blueprint. For the database, use Render's **Internal Database URL** directly in `DATABASE_URL` (it normally starts with `postgresql://`). The application converts that Render URL to JDBC and extracts its username/password. Do not type `DATABASE_HOST`, `<internal-db-host>`, or any other placeholder literally:

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=<paste-the-real-Internal-Database-URL-from-Render>
SESSION_COOKIE_SECURE=true
SMARTLIB_BOOTSTRAP_ADMIN_USERNAME=<one-time-admin-username>
SMARTLIB_BOOTSTRAP_ADMIN_PASSWORD=<one-time-random-password-at-least-16-characters>
SMARTLIB_BOOTSTRAP_ADMIN_EMAIL=<admin-email>
SMARTLIB_BOOTSTRAP_ADMIN_FULL_NAME=<admin-name>
```

Paste the complete real Internal Database URL from Render into `DATABASE_URL`; do not paste these values into GitHub. If you prefer separate values, you may instead set `DB_URL` to a real `jdbc:postgresql://...` URL and set `DB_USERNAME` and `DB_PASSWORD` separately. The application accepts Render's injected `PORT` automatically and exposes `/actuator/health` for the health check.

5. Deploy the service and wait for `/actuator/health` to report `UP`.
6. Open `/login` on the generated Render URL and sign in with the one-time bootstrap values.
7. After the first successful login, remove the four `SMARTLIB_BOOTSTRAP_ADMIN_*` variables and redeploy. The created account remains in PostgreSQL.
8. Test catalog search, issue, return, renewal, fine payment, and `/swagger-ui.html`.

The Docker build uses the official Maven image and a single retrying dependency-resolution pass. This avoids downloading Maven through the wrapper during a Render build and backs off when Maven Central temporarily returns HTTP 429.

### 2. Deploy the visual preview on Vercel

1. In Vercel, choose **Add New → Project** and import `tarun-tech-tech/library-management`.
2. Keep the repository root as the project root.
3. Framework preset: **Other**.
4. Build command: leave empty.
5. Output directory: `public`.
6. Deploy.

The Vercel URL is the static design preview. It is intentionally separate from the functional Render backend because Vercel does not run the Java/Spring Boot container in this repository. If you later want the Vercel UI to call the backend, add a configurable API origin, CORS policy, and a production session strategy before connecting the two origins.

### 3. First production checklist

- Confirm the Render URL serves `/actuator/health` with `UP`.
- Confirm PostgreSQL migrations V1, V2, and V3 ran in the deployment logs.
- Sign in, then remove bootstrap variables.
- Set a custom domain and HTTPS before enabling real users.
- Configure backups for managed PostgreSQL and test a restore.
- Keep the Vercel preview and backend URLs documented for the team.

## Security and deployment notes

- Never commit `.env`, bootstrap credentials, API keys, SMTP passwords, or database dumps.
- Set `SESSION_COOKIE_SECURE=true` when HTTPS is guaranteed at the production edge.
- Provide a managed PostgreSQL URL and keep Flyway enabled.
- Use a platform that supports Java containers for the backend, such as Render, Fly.io, Railway, Cloud Run, or a managed Kubernetes service. Use Vercel for the static preview unless the deployment architecture is changed.
- Create the initial administrator through the one-time environment bootstrap, then remove or rotate the bootstrap variables after the account exists.
- Back up PostgreSQL with the provider's encrypted backup facility and test restore procedures before production use.

## Legacy application

The original Ant application is preserved under `src/java`, `web`, `database`, `lib`, and `nbproject`. It targets GlassFish 7, Jakarta EE 10, EclipseLink, and MySQL. Its known limitations include no server-side authentication, direct database access from JSPs, schema/configuration drift, and no automated regression suite. Do not expose it as the production SmartLib service.

## Checks and known gaps

The last verified commands from the repository root are:

```text
.\mvnw.cmd -B -ntp test     PASS: 6 tests
.\mvnw.cmd -B -ntp package  PASS: executable Spring Boot JAR created
```

Docker/PostgreSQL startup and browser screenshots are not claimed as locally verified when those tools/services are unavailable. See `SMARTLIB_DESIGN_AND_STATUS.md` for the detailed handoff and the next implementation slice.
