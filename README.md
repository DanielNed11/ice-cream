# Nano Protein Ice Cream — API

Spring Boot backend for a fictional protein ice cream shop: catalog, cart,
checkout, order history, transactional email and an admin panel. Built as a
portfolio project, but written to production practice — the interesting parts
are the concurrency handling, not the CRUD.

The storefront that consumes this API lives in a separate repository
(Next.js): <https://github.com/DanielNed11/ice-cream-project>

## Stack

| | |
|---|---|
| Language / runtime | Java 25, Spring Boot 4.1 |
| Data | PostgreSQL 17, Spring Data JPA (Hibernate 7), Liquibase |
| Security | Spring Security 7, JWT access tokens with rotating refresh tokens |
| Email | JavaMail + Thymeleaf templates, Mailpit in dev, Gmail SMTP in prod |
| Scheduling | Spring scheduling on virtual threads, ShedLock for multi-instance locking |
| Reporting | Apache POI (SXSSF) streaming Excel export |
| Tests | JUnit 5 against a real Postgres container (67 tests) |

## What it does

**Storefront** — browse products, a per-customer cart, and a checkout that
decrements stock with an atomic conditional update and snapshots each product's
name and price onto the order line, so an order is never rewritten by a later
price change.

**Orders** — paginated history, detail and customer cancellation. Cancelling
restores stock through a compare-and-set status transition, so a double
cancellation cannot restore stock twice. Orders are addressed by a stored,
unique reference (`#B8B20784`) rather than their UUID.

**Email** — multipart HTML and plain text confirmation, delivered and
cancellation emails, sent after the transaction commits on a virtual thread, so
a mail outage can never fail a purchase. Every attempt, sent or failed, is
recorded in `sent_email`.

**Scheduled jobs** — a daily sweep marks orders delivered (marking *before*
sending, so an order cancelled mid-run never receives a delivery email), and a
nightly restock tops up active products. Both are locked across instances with
ShedLock.

**Admin** — order analytics under a repeatable-read snapshot, status and date
filtering, and a streamed `.xlsx` export that reads pages with a keyset cursor
rather than OFFSET, so a checkout during the export cannot duplicate rows.

## Running it locally

Requires Docker and JDK 25.

```bash
docker compose up -d                 # Postgres on 5432, Mailpit on 1025/8025
cp .env.example .env                 # then fill in the values below
./gradlew bootRun
```

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Mailpit (catches every email sent in dev): <http://localhost:8025>

### Environment

`.env` is gitignored. The application reads:

| Variable | Used for |
|---|---|
| `DATASOURCE_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/ice-cream` |
| `DATASOURCE_PASSWORD` | Database password |
| `JWT_SECRET` | **Base64-encoded** signing key, at least 256 bits |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP account (a Gmail App Password in prod) |
| `FRONTEND_URL` | Link target in emails |
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins allowed to call the API |

Generate a usable JWT secret with:

```bash
openssl rand -base64 48
```

### Tests

The suite runs against its own database on port 5434, so it never touches your
development data:

```bash
docker compose -f docker-compose.test.yml up -d
./gradlew test
```

## Deploying (Render)

The `Dockerfile` is a two-stage build producing a JRE image that runs as a
non-root user.

1. Create a **Web Service** from this repository, environment **Docker**.
2. Health check path: `/actuator/health`
3. Set the environment variables from the table above, plus
   `SPRING_PROFILES_ACTIVE=prod`.
4. `PORT` is provided by Render and is read automatically.

Point `CORS_ALLOWED_ORIGINS` at your deployed frontend origin, and set the
frontend's `NEXT_PUBLIC_API_URL` to this service's URL.

## Trying the admin panel

Registration always creates a `CUSTOMER` — a client cannot ask for a role, so
there is no way to grant yourself admin access through the API.

If you would like to look at the admin panel (order analytics, filtering and
the spreadsheet export) or the superadmin product management, register an
account on the demo and contact me, and I will promote it for you.

## Notes on the design

A few decisions that are deliberate rather than accidental:

- **Stock is only ever written as a delta** (`stock = stock - :qty`), never as
  an absolute value read earlier, so concurrent checkouts compose instead of
  overwriting each other. The admin product form cannot set stock for exactly
  this reason.
- **Checkout takes a pessimistic lock on the cart row** so a double-submitted
  checkout produces one order, and locks product rows in a deterministic order
  so two concurrent checkouts cannot deadlock.
- **Mail never gates a purchase.** Emails are sent after commit, on a virtual
  thread, and the mail health indicator is disabled so an SMTP outage cannot
  mark the service unhealthy.
- **The Excel export uses keyset paging**, because each page is its own short
  transaction and an OFFSET would be computed against a different snapshot each
  time.
