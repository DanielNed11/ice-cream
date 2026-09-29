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
| Email | Thymeleaf templates, SMTP to Mailpit in dev, Resend's HTTPS API in prod |
| Scheduling | Spring scheduling on virtual threads, ShedLock for multi-instance locking |
| Reporting | Apache POI (SXSSF) streaming Excel export |
| Tests | JUnit 5 against a real Postgres container (71 tests) |

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
recorded in `sent_email`. A `MailTransport` seam separates the message from the
channel that carries it, so development keeps a local mail catcher while
production posts to an HTTP API, and sends carry an idempotency key so a
replayed event cannot deliver the same email twice.

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
| `MAIL_USERNAME` | Sender address in development; Mailpit accepts anything |
| `MAIL_FROM` | Sender address in production, on a domain verified in Resend |
| `RESEND_API_KEY` | Resend API key; production only |
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

## Deploying (Railway)

Railway builds the `Dockerfile` in this repository and runs the image, so there
is nothing platform-specific in the application itself.

1. New project → **Deploy from GitHub repo** → this repository.
2. Railway detects the `Dockerfile`; `railway.json` sets the health check to
   `/actuator/health`.
3. Add the environment variables from the table above, plus
   `SPRING_PROFILES_ACTIVE=prod`.
4. `PORT` is injected by Railway and is read automatically.

Three things that are easy to get wrong:

- **Outbound SMTP does not work.** Railway drops connections on ports 25, 465,
  587 and 2525, to every destination and on both address families; only 443
  gets out. A mail library sees this as a connect timeout, which looks like bad
  credentials and is not — authentication happens several phases later and that
  code never runs. This is why production sends over HTTPS rather than SMTP.

- **`DATASOURCE_URL` must be set and reachable.** The application connects
  during startup to run migrations, so an unset or unreachable database means
  the process never finishes booting and never opens a port. The platform
  reports that as "no open ports", which points at the wrong problem.
- **`CORS_ALLOWED_ORIGINS` must list the deployed frontend's origin**, not
  `localhost`, or the browser blocks every request while both services look
  healthy.

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
  thread, and the mail health indicator is disabled so a mail outage cannot
  mark the service unhealthy. This was not theoretical: production spent a day
  unable to send anything, and the only casualties were log lines and
  `sent_email` rows marked `FAILED`.
- **The Excel export uses keyset paging**, because each page is its own short
  transaction and an OFFSET would be computed against a different snapshot each
  time.
