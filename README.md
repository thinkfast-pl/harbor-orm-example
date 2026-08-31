# harbor-orm-example

Example Spring Boot application showing how to use the
[HarborORM](https://github.com/thinkfast-pl/harbor-orm) library.

A small shop-like domain (users, roles, customers, products, orders, documents and
order reports) is mapped with HarborORM and exercised through integration tests. The
same domain model runs unchanged against H2, PostgreSQL, MariaDB and MySQL: each
database gets its own Liquibase changelog under `src/main/resources/db/changelog/`,
and the HarborORM dialect is picked at runtime from the configured `DataSource`
(see `HarborConfiguration`).

The code covers identity, sequence and client-assigned IDs, a composite embedded ID,
embeddables with attribute overrides, element collections, many-to-many relations,
a lazy one-to-one reference, optimistic locking, attribute converters, JSON columns,
CLOB/BLOB handling, lifecycle callbacks, a database view, a stored function and
hand-written queries with joins, a CTE and multiset aggregation.

## Requirements

- Java 25 (`.sdkmanrc` is provided, so `sdk env` will select it)
- Docker, for the test suites that run on Testcontainers

## Running the tests

Each database has its own Gradle test suite:

| Task | Database | Needs Docker |
|------|----------|--------------|
| `./gradlew testH2` | in-memory H2 | no |
| `./gradlew testPostgres` | PostgreSQL (Testcontainers) | yes |
| `./gradlew testMariadb` | MariaDB (Testcontainers) | yes |
| `./gradlew testMysql` | MySQL (Testcontainers) | yes |
| `./gradlew test` | Spring context smoke test on PostgreSQL (Testcontainers) | yes |

`./gradlew check` runs all of them.

## License

Licensed under the Apache License, Version 2.0 — see [LICENSE](LICENSE).
