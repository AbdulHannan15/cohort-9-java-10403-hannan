# Contact Management System

A full-stack web application for managing personal contacts — self-registration by email or
phone, JWT-secured login, and full CRUD over contacts with multiple labeled emails and phone
numbers per contact, paginated and searchable.

Built with **Spring Boot 4 / Spring Security 7** on the backend (MySQL, JWT auth, Spring Data
JPA) and **React (Vite)** on the frontend.

```
.
├── backend/     Spring Boot REST API
└── frontend/    React (Vite) single-page app
```

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Architecture](#architecture)
- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend setup](#backend-setup)
  - [Frontend setup](#frontend-setup)
- [Environment variables](#environment-variables)
- [API reference](#api-reference)
- [Authentication flow](#authentication-flow)
- [Testing](#testing)
- [SonarQube](#sonarqube)
- [Screens](#screens)
- [Known limitations / not yet implemented](#known-limitations--not-yet-implemented)

---

## Features

**Authentication & authorization**
- Self-registration using either an email address or a phone number as the login identifier
- Login returns a signed JWT; every subsequent request is authenticated statelessly via that token
- Change password (requires the current password)
- Passwords are hashed with BCrypt, never stored or returned in plaintext

**Contact management**
- Create, view, update, and delete contacts
- Each contact has a first name, last name, title (Mr/Mrs/Ms/Dr/Prof), and any number of labeled
  emails (Work/Personal/Education) and phone numbers (Personal/Office/Home/WhatsApp/Call
  only/Emergency only)
- Paginated listing
- Search/filter by first or last name (case-insensitive, partial match)
- Every contact is scoped to its owning user — one user can never read, edit, or delete another
  user's contacts, even by guessing an ID

**Cross-cutting**
- Centralized exception handling → consistent JSON error responses with proper HTTP status codes
- Structured logging via Slf4j/Logback
- CORS configured for the frontend origin

---

## Tech stack

| Layer | Technology |
|---|---|
| Backend framework | Spring Boot 4.0.7 (Spring Framework 7.0.8) |
| Security | Spring Security 7, JWT (jjwt) |
| Persistence | Spring Data JPA + Hibernate 7, MySQL 8 |
| Build | Maven |
| Backend tests | JUnit 5, Mockito, AssertJ, MockMvc, `@DataJpaTest` (H2 in-memory) |
| Frontend framework | React 18, Vite |
| Frontend routing | React Router |
| Frontend HTTP | Axios |
| Frontend tests | Vitest, React Testing Library |
| Code quality | SonarQube, JaCoCo (coverage) |
| Logging | Slf4j / Logback |

---

## Project structure

```
backend/
└── src/main/java/com/Contact/Management/System/Cms/
    ├── CmsApplication.java          Entry point
    ├── Entity/                      JPA entities: User, Contact, EmailEntity, PhoneNumberEntity
    ├── SupportingEnum/              RoleEnum, NameTitle, EmailType, PhoneNumberType
    ├── DTO/                         Request/response objects (never expose entities directly)
    ├── Repo/                        Spring Data JPA repositories
    ├── Service/                     Business logic (UserService, ContactService)
    ├── Security/                    JWT generation/validation, UserDetails adapter, auth filter
    ├── Config/                      SecurityConfig (stateless JWT filter chain, CORS, PasswordEncoder)
    ├── Controller/                  REST endpoints (AuthController, UserController, ContactController)
    └── Exception/                   Custom exceptions + global exception handler
└── src/test/java/...                Mirrors the structure above

frontend/
└── src/
    ├── api/                         Axios client + typed API call wrappers
    ├── context/                     AuthContext (JWT storage, current user state)
    ├── components/                  Navbar, ContactFormModal, DeleteConfirmModal, ProtectedRoute
    ├── pages/                       LoginPage, RegisterPage, ContactsPage, ProfilePage
    └── App.jsx                      Routing
```

---

## Architecture

**Request flow for a protected endpoint** (e.g. fetching a contact list):

1. Client sends `GET /api/contacts` with header `Authorization: Bearer <token>`
2. `JwtAuthenticationFilter` intercepts the request, validates the token via `JwtService`, loads
   the user via `CustomUserDetailsService`, and marks the request as authenticated in Spring
   Security's `SecurityContext`
3. `ContactController` reads the authenticated user via `@AuthenticationPrincipal`, never trusting
   any user ID passed in the request itself
4. `ContactService` enforces ownership — every read/write is scoped to `userId`, so a contact
   belonging to another user returns 404, not 403 (doesn't leak whether the resource exists)
5. `ContactRepo` (Spring Data JPA) executes the query against MySQL
6. Response flows back through the same chain as JSON

**Login flow:**

1. Client sends `POST /api/auth/login` with `loginIdentifier` + `password` (this endpoint is
   `permitAll` — no token required)
2. `AuthenticationManager` delegates to `CustomUserDetailsService` (loads the user) and
   `PasswordEncoder` (verifies the hash)
3. On success, `JwtService` signs a new token containing the user ID and role as claims
4. Client stores the token (currently `localStorage` on the frontend) and attaches it to every
   future request

---

## Getting started

### Prerequisites

- Java 17+ (project targets Java 17, tested against JDK 21)
- Maven 3.8+
- Node.js 18+
- A running MySQL 8+ instance

### Backend setup

1. **Configure the database.** Edit `backend/src/main/resources/application.properties`, or set
   environment variables:

   | Variable | Default | Purpose |
   |---|---|---|
   | `DB_HOST` | `localhost` | MySQL host |
   | `DB_PORT` | `3306` | MySQL port |
   | `DB_NAME` | `contact_management_system` | Database name (auto-created if missing) |
   | `DB_USER` | `root` | DB username |
   | `DB_PASSWORD` | `root` | DB password |
   | `JWT_SECRET` | *(a local-dev default is checked in)* | Base64, 32+ bytes — generate your own for anything beyond local dev with `openssl rand -base64 32` |

   Fastest path to a local MySQL: `docker compose up -d mysql` from `backend/` (see
   `backend/docker-compose.yml`) — matches the `root`/`root` defaults above.

2. **Run it:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   API comes up on `http://localhost:8080`. Schema is auto-created/updated on startup
   (`spring.jpa.hibernate.ddl-auto=update`) — no manual DDL needed.

### Frontend setup

1. **Configure the API URL** — `frontend/.env`:
   ```
   VITE_API_BASE_URL=http://localhost:8080/api
   ```
   Already set to match the backend's default port and path.

2. **Run it:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Opens on `http://localhost:5173`.

---

## Environment variables

All backend configuration is externalized so nothing sensitive is hardcoded:

```properties
# MySQL
DB_HOST=localhost
DB_PORT=3306
DB_NAME=contact_management_system
DB_USER=root
DB_PASSWORD=root

# JWT
JWT_SECRET=<your-own-base64-secret-32-bytes-or-more>
```

`src/main/resources/application.properties` is deliberately excluded from version control
(`.gitignore`) since it's the file that will eventually hold real credentials — set these via
your environment or a secrets manager in any shared environment.

---

## API reference

All endpoints are prefixed with `/api`.

| Method | Path | Auth required | Description |
|---|---|---|---|
| `POST` | `/auth/register` | No | Register with an email or phone as the login identifier |
| `POST` | `/auth/login` | No | Returns a JWT on success |
| `GET` | `/users/me` | Yes | Current user's profile |
| `PUT` | `/users/me/password` | Yes | Change password (requires current password) |
| `GET` | `/contacts?page=&size=&search=` | Yes | Paginated list, optionally filtered by name |
| `POST` | `/contacts` | Yes | Create a contact |
| `GET` | `/contacts/{id}` | Yes | Get a single contact (must be owned by the caller) |
| `PUT` | `/contacts/{id}` | Yes | Update a contact |
| `DELETE` | `/contacts/{id}` | Yes | Delete a contact |

Protected endpoints require header: `Authorization: Bearer <token>`

**Example — register:**
```json
POST /api/auth/register
{
  "loginIdentifier": "jane@example.com",
  "password": "a-strong-password",
  "recoveryPhone": "+15551234567"
}
```

**Example — create contact:**
```json
POST /api/contacts
{
  "firstName": "Jane",
  "lastName": "Doe",
  "title": "MS",
  "emails": [{ "email": "jane@work.com", "type": "WORK" }],
  "numbers": [{ "number": "+15551234567", "numberType": "OFFICE" }]
}
```

**Error responses** are consistent JSON, e.g.:
```json
{
  "timestamp": "2026-08-30T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Contact not found with id: 100"
}
```

---

## Authentication flow

Stateless JWT — no server-side session. Rough sequence:

```
Client                AuthController        AuthenticationManager        JwtService
  │  POST /auth/login       │                        │                       │
  ├─────────────────────────▶                        │                       │
  │                         │  authenticate()         │                       │
  │                         ├──────────────────────────▶                      │
  │                         │        (verifies via CustomUserDetailsService   │
  │                         │         + PasswordEncoder)                      │
  │                         │◀──────────────────────────                     │
  │                         │  generateToken()                                │
  │                         ├──────────────────────────────────────────────▶  │
  │                         │◀──────────────────────────────────────────────  │
  │◀─────────────────────────  { token, userId, role }                       │
  │                                                                            │
  │  GET /contacts  (Authorization: Bearer <token>)                          │
  ├────────────────────────────────────────────────────────────────────────▶ │
  │              JwtAuthenticationFilter validates + loads user,             │
  │              marks request authenticated, controller runs                │
  │◀──────────────────────────────────────────────────────────────────────── │
```

The token carries the user's ID and role as claims; controllers read the authenticated user via
`@AuthenticationPrincipal CustomUserDetails` rather than trusting any ID sent by the client.

---

## Testing

### Backend
```bash
cd backend
mvn test
```
Runs against an isolated in-memory H2 database — never touches your real MySQL instance.

| Layer | Test class | Covers |
|---|---|---|
| Service | `UserServiceImplTest` | register, login, changePassword, getUserById — every success and failure branch |
| Service | `ContactServiceImplTest` | full CRUD, ownership enforcement, pagination, search fallback, duplicate checks |
| Security | `JwtServiceTest` | token generation, claim extraction, validity, expiry, malformed tokens |
| Controller | `AuthControllerTest` | register, login, bad-credentials handling |
| Controller | `UserControllerTest` | `/me`, change password, scoped to the authenticated principal |
| Controller | `ContactControllerTest` | all 5 REST verbs, search vs. plain listing |
| Repository | `UserRepoTest`, `EmailRepoTest`, `NumberRepoTest` | lookups, existence checks, unique constraints |
| Repository | `ContactRepoTest` | pagination, case-insensitive name search, per-user scoping |

Not unit-tested: `Entity/`, `DTO/`, `SupportingEnum/` — plain data holders with no logic of their
own.

### Frontend
```bash
cd frontend
npm test              # single run
npm run test:coverage # generates coverage/lcov.info for SonarQube
```

| Test file | Covers |
|---|---|
| `LoginPage.test.jsx` | Field rendering, submit flow, navigation on success, server error display |
| `RegisterPage.test.jsx` | Password-confirmation validation, register→auto-login→navigate flow |
| `ContactFormModal.test.jsx` | Create vs. edit pre-population, dynamic email rows, save payload shape |
| `DeleteConfirmModal.test.jsx` | Confirm/cancel callbacks, disabled state while deleting |

---

## SonarQube

The backend has SonarQube wired in via `pom.xml` (project key, JaCoCo coverage report path,
exclusions) and JaCoCo for coverage collection.

```bash
cd backend
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<your-generated-token>
```

To spin up a local SonarQube instance for testing:
```bash
cd backend
docker compose up -d
```
Comes up on `http://localhost:9000` (default login `admin`/`admin`, you'll be prompted to change
it). Generate a token under **My Account → Security**.

A root-level `sonar-project.properties` is also included as an alternative if you prefer running
the generic `sonar-scanner` CLI instead of the Maven plugin.

---

## Screens

- **Login** — email/phone + password
- **Register** — email/phone, password, confirm password, optional recovery phone
- **Contacts** — paginated table, search bar, create/edit/delete actions
- **Create/edit contact modal** — first name, last name, title, dynamically add/remove multiple
  labeled emails and phone numbers
- **Delete confirmation modal**
- **Profile** — account details, logout, change password

---

## Known limitations / not yet implemented

- **Export/Import contacts** — listed as an optional/stretch feature in the original requirements, not built
- **No refresh token** — JWTs expire (`jwt.expiration-ms`, default 24h) and there's no silent
  refresh flow yet; the user is simply logged out and redirected to `/login` on a 401
- **No CI/CD pipeline** — running `mvn verify sonar:sonar` and `npm run test:coverage` on every PR
  is left for you to wire up (GitHub Actions, GitLab CI, etc.)
- **No rate limiting** on `/api/auth/login` or `/api/auth/register`
- **CORS is currently wide open** (`allowedOriginPatterns: ["*"]`) — tighten this to your actual
  frontend origin before deploying anywhere real
