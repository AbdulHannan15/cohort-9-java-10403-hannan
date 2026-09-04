# cohort-9-java-10403-hannan
Cohort 9 — JAVA Fullstack (JAVA+ReactJS) assignment for Hannan Fiaz

## Running the tests

```bash
mvn test
```

Tests run against an isolated in-memory H2 database (`src/test/resources/application.properties`)
— they never touch your real MySQL instance. Coverage:

| Layer | File | What's covered |
|---|---|---|
| Service | `UserServiceImplTest` | register, login, changePassword, getUserById — success + every failure branch |
| Service | `ContactServiceImplTest` | full CRUD, ownership enforcement, pagination, search-vs-listing fallback, duplicate checks |
| Security | `JwtServiceTest` | token generation, claim extraction, validity, expiry, malformed tokens |
| Controller | `AuthControllerTest` | register 200, login 200 + token shape, login 401 on bad credentials |
| Controller | `UserControllerTest` | `/me` and change-password, scoped to the authenticated principal |
| Controller | `ContactControllerTest` | all 5 REST verbs, plus `?search=` vs. plain listing |
| Repository | `UserRepoTest`, `EmailRepoTest`, `NumberRepoTest` (`@DataJpaTest`) | lookups, existence checks, unique-constraint enforcement |
| Repository | `ContactRepoTest` (`@DataJpaTest`) | pagination, case-insensitive name search, per-user scoping |

**Note on `@MockitoBean`**: this project targets Spring Boot 4, which *removed* `@MockBean`
entirely (not just deprecated it) in favor of `@MockitoBean` from
`org.springframework.test.context.bean.override.mockito`. If you're used to older Spring Boot
tutorials showing `@MockBean`, that annotation will not compile here.

**Note on JSON in controller tests**: request bodies are built as raw JSON text blocks rather
than via an autowired `ObjectMapper`. This project has both Jackson 3 (Spring Boot 4's default)
and classic Jackson 2 (pulled in transitively by `jjwt-jackson`) on the classpath — using text
blocks sidesteps any ambiguity about which `ObjectMapper` bean gets autowired.

Not unit-tested: `Entity/`, `DTO/`, `SupportingEnum/` classes — plain data holders with no logic
of their own.

## SonarQube

```bash
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<your-generated-token>
```

`mvn verify` runs the tests and produces `target/site/jacoco/jacoco.xml` (via the
`jacoco-maven-plugin` in `pom.xml`); `sonar:sonar` picks it up automatically along with the
`sonar.*` properties already set in `pom.xml` — you only need to supply the host URL and token.

To spin up a local SonarQube (and MySQL) for testing this yourself:
```bash
docker compose up -d
```
SonarQube comes up on `http://localhost:9000` (default login `admin`/`admin`, you'll be prompted
to change it on first login). Generate a token under **My Account → Security** and use it above.

