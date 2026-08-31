package io.harbor.example;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Statement;
import java.util.List;

// CASE_INSENSITIVE_IDENTIFIERS: HarborORM quotes lowercase identifiers, the schema is created unquoted.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:harbor;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE")
public abstract class H2IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void truncateAllTables() {
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = 'PUBLIC'
                  AND table_type = 'BASE TABLE'
                  AND LOWER(table_name) NOT IN ('databasechangelog', 'databasechangeloglock')
                """, String.class);
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET REFERENTIAL_INTEGRITY FALSE");
                try {
                    for (String table : tables) {
                        statement.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY");
                    }
                } finally {
                    statement.execute("SET REFERENTIAL_INTEGRITY TRUE");
                }
            }
            return null;
        });
    }
}
