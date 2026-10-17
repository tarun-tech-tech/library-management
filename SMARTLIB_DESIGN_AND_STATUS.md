# SmartLib design and implementation record

This file is the running handoff for SmartLib. It records why the current design choices were made, what has actually been implemented, what has been verified, and what still needs engineering work. It intentionally separates a real application behavior from the Vercel visual preview.

## Product direction

SmartLib is being rebuilt as a real library operations system rather than a dashboard-only prototype. The first useful user flow is:

1. A librarian signs in through a protected session.
2. They search the catalog and inspect a physical copy.
3. They issue, renew, or return a copy through the transactional backend.
4. They record a fine payment with a method and optional reference when authorized.
5. The member, due date, availability, and fine state remain consistent when the page is reopened.

The old Ant/GlassFish/MySQL application is preserved. The new Spring Boot/PostgreSQL application is deliberately added beside it so that the legacy behavior can be compared and migrated without deleting useful context.

## Deterministic design selection

The requested design process required a deterministic Python-style selection before UI work. The literal seed expression used for the build was:

```python
seed = len("SmartLib: secure library circulation, catalog intelligence, and an editorial dark-room dashboard")
```

The actual Python run returned `96` and selected:

- Hero: **Artistic Asymmetry**.
- Font: **Outfit**.
- Component architectures: **Infinite marquee**, **feedback carousel**, and **horizontal accordions**.
- GSAP paradigms: **image scale/fade** and **ScrollTrigger pinning**.

Those results, rather than the first option in a static list, drive the current page composition. The feedback carousel is the quote card in the dashboard story rail. The horizontal accordion is in the catalog discovery section. The marquee is the lime principle band. The image scale/fade and pinned story title are implemented in `static/js/smartlib.js` and are gated behind reduced-motion checks.

## Visual system decisions

### Color

The interface uses a dark navy-black ink (`#0c1013`) for navigation, secure actions, and the narrative story band. A warm paper ground (`#f3f4ef`) prevents the workspace from feeling like a generic SaaS admin panel. Acid lime (`#c8ee65`) is reserved for active states and positive movement, while red and pale blue provide distinct operational signals.

This palette creates contrast without relying on gradients for hierarchy. Ambient radial washes are used only over image surfaces where they improve legibility.

### Typography

Outfit was the deterministic selection. It is used for the display and interface system because its geometric forms stay readable in dense tables while still giving the hero a clean editorial voice. The H1 uses a wide, fluid measure with `clamp()` sizing and a broad `max-w-6xl` treatment. The selected hero copy is short enough to hold to two or three lines rather than becoming a narrow text wall.

### Layout

The application shell uses a persistent desktop rail and a compact mobile navigation. The hero deliberately breaks the repeated left-text/right-card pattern by letting the wide image bleed leftward into the text column. Major chapters receive substantial vertical padding so the catalog, circulation, and recommendation layers read as distinct moments.

The dashboard bento uses four columns and three rows with `grid-auto-flow: dense`:

```text
2x2 lead panel  = 4 cells
four 1x1 panels  = 4 cells
two 2x1 panels   = 4 cells
occupied         = 12 cells
grid             = 4 columns x 3 rows = 12 cells
occupancy        = 12 / 12 = 100%
```

At the mobile breakpoint every span collapses to one column. This keeps the layout dense without creating unusable narrow cards.

### Assets and motion

Images use context-specific Picsum seeds such as `library-architecture`, `library-shelf`, `library-stacks`, and `library-night`. They are treated with grayscale and contrast filters so the photographic language stays coherent. Every image container clips its media and provides a scale response on hover.

The default motion system uses GSAP and ScrollTrigger when the libraries are available. The script animates section entrances, grows images from `scale: .8` to `scale: 1`, darkens them as they leave the view, and pins the story title on wide screens. Under `prefers-reduced-motion: reduce`, pinning, scrubbing, scale/fade motion, and the marquee collapse to a stable static state.

## Backend decisions

### Why Spring Boot and Maven

The requested production stack is Java 21, Spring Boot, Thymeleaf, Spring Data JPA, Flyway, PostgreSQL, and Docker. The root Maven project keeps the backend Java-based and removes the old JSP scriptlet/direct-EntityManager coupling without introducing a mandatory Node backend.

Maven Wrapper 3.9.11 is included so the build does not depend on a global Maven installation. `.java-version` and the compiler release pin the intended Java level.

### Why PostgreSQL and Flyway

The new service uses PostgreSQL and an explicit Flyway migration. Hibernate validates the schema rather than silently recreating it. This prevents the database/entity drift seen in the legacy application and keeps schema changes reviewable.

The original project still uses MySQL because it is a separate legacy application. Its hard-coded persistence credential and schema mismatch are not copied into the new service.

### Authentication and access

Authentication uses BCrypt, Spring Security sessions, CSRF tokens, session fixation migration, and role checks for administrator, librarian, and member operations. Browser pages use a normal login route while API failures remain JSON responses.

There is no public administrator registration and no predictable default account. A bootstrap administrator is created only when all four `SMARTLIB_BOOTSTRAP_ADMIN_*` environment values are supplied, and the password must contain at least 16 characters. After the first account exists, the bootstrap variables should be removed or rotated.

### Circulation integrity

Issue, return, and renewal rules live in `LoanService`. Issue locks the physical copy and related book row, checks active membership and availability, updates copy state and book availability in one transaction, and assigns a configured due date. Returns restore availability and create a decimal-safe fine when the configured due date has passed. Duplicate issue attempts cannot pass the copy-level lock and availability check in the same way as an unlocked read-modify-write flow.

### Deterministic reading signals

External AI integration is intentionally out of scope for this build. The current reading-signal strategy is deliberately deterministic: it ranks real, available catalog records using a member's category/author history when a member is linked, then falls back to available catalog data. This keeps the library useful without API credentials, paid requests, external privacy risk, or fabricated catalog results. No chatbot or external model endpoint is included in the current application scope.

## Files created or changed in this slice

### Application foundation

- `pom.xml`, `.java-version`, Maven Wrapper files, `Makefile`, `.env.example`, `Dockerfile`, and `compose.yaml`.
- `src/main/resources/application.yml` with environment-driven database, session, loan, fine, and bootstrap settings.
- `src/main/resources/db/migration/V1__initial_schema.sql` with users, members, books, copies, loans, fines, recommendations, and JDBC session tables.
- `src/main/resources/db/migration/V2__fine_payments.sql` with partial-payment tracking and authorized payment records.
- `src/main/resources/db/migration/V3__copy_statuses.sql` aligning physical copies with `AVAILABLE`, `ISSUED`, `RESERVED`, `LOST`, `DAMAGED`, and `RETIRED` states.

### Backend domains

- Entities and enums under `src/main/java/com/smartlib/domain`.
- Repositories under `src/main/java/com/smartlib/repository`.
- Services under `src/main/java/com/smartlib/service`.
- Versioned REST controllers and DTOs under `src/main/java/com/smartlib/api`.
- Session and role configuration under `src/main/java/com/smartlib/config/SecurityConfig.java`.

### Web workspace

- Thymeleaf fragments under `src/main/resources/templates/fragments`.
- Login, dashboard, catalog, book detail, circulation, and recommendations pages under `src/main/resources/templates`.
- Fine ledger page for member balances and authorized staff payment recording.
- `src/main/resources/static/css/smartlib.css` for the visual system.
- `src/main/resources/static/js/smartlib.js` for API actions, accordion/carousel behavior, GSAP motion, CSRF handling, and reduced-motion fallback.

### Vercel preview

- `public/index.html`, `public/preview.css`, and `public/preview.js`.
- `vercel.json` configures the static preview output directory.

The preview is intentionally not presented as the real backend. Its sample metrics are visibly described as illustrative in the page and README.

## Verification performed

From the workspace root, these commands completed successfully after the current changes:

```text
.\mvnw.cmd -B -ntp test
PASS: 4 tests, 0 failures, 0 errors

.\mvnw.cmd -B -ntp package
PASS: executable JAR created at target/smartlib-backend-0.1.0-SNAPSHOT.jar
```

The successful tests cover fine calculation for on-time and overdue cases plus partial-payment, full-payment, and overpayment boundaries. The Java compiler also validated the new web controller, dashboard service, repositories, payment model, and application dependencies.

Not locally verified:

- Docker image build and Compose startup because Docker is not available in this workspace.
- PostgreSQL migration execution against a live database because PostgreSQL tools/service are not available here.
- Browser screenshots and real GSAP runtime inspection because no browser run was performed in this slice.
- Vercel deployment itself because it requires the user's Vercel account and network access.

The backend is prepared for platforms that inject a `PORT` environment variable, including Render. The recommended public deployment is a Render Docker web service plus managed PostgreSQL, with Vercel used for the static preview.
The repository also contains `render.yaml` so Render can create the Docker service configuration without copying the health-check and environment-variable names by hand.

## Remaining work

The next engineering slices should be implemented and checked in this order:

1. Add integration tests with Testcontainers for Flyway, authentication, role boundaries, catalog persistence, issue/return, fines, and concurrency.
2. Add member/librarian/admin management pages and password reset/activation flows.
3. Add reservations and a durable notification/outbox model with email provider configuration.
4. Add barcode/QR generation, printable labels, scanner fallback, and PDF/Excel/CSV exports.
5. Add dashboard query projections, health details, audit logs, and settings with allow-listed keys.
6. Add OpenAPI documentation and complete operational settings, health details, and privacy-safe audit records.
7. Build and run the Docker/Compose path on a machine with Docker, then deploy the backend to a Java container platform and deploy `public/` to Vercel.

No item above should be described as complete until its real check has run.
