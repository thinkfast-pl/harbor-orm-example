package io.harbor.example;

import io.harbor.api.HarborSession;
import io.harbor.api.converter.JsonSerializer;
import io.harbor.core.HarborSessionFactory;
import io.harbor.core.sql.RdbmsSupport;
import io.harbor.core.sql.SqlConnectionAccessor;
import io.harbor.core.sql.SqlConnectionFunction;
import io.harbor.h2.dialect.H2RdbmsSupport;
import io.harbor.mariadb.dialect.MariaDbRdbmsSupport;
import io.harbor.mysql.dialect.MySqlRdbmsSupport;
import io.harbor.mysql.dialect.MySqlStoredProcedureSequenceGeneratorHandler;
import io.harbor.postgres.dialect.PostgreSqlRdbmsSupport;
import lombok.NonNull;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Configuration
class HarborConfiguration {

    @Bean
    HarborSession harborSession(DataSource dataSource, ObjectMapper objectMapper) {
        SqlConnectionAccessor accessor = new SqlConnectionAccessor() {

            @Override
            public <R> R execute(@NonNull SqlConnectionFunction<R> function) {
                Connection connection = DataSourceUtils.getConnection(dataSource);
                try {
                    return function.execute(connection);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } finally {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            }
        };

        JsonSerializer jacksonSerializer = new JsonSerializer() {

            @Override
            public String serialize(Object value) {
                return objectMapper.writeValueAsString(value);
            }

            @Override
            public <T> T deserialize(String json, Class<T> type) {
                return objectMapper.readValue(json, type);
            }
        };

        return HarborSessionFactory.builder()
                .connectionAccessor(accessor)
                .rdbmsSupport(rdbmsSupport(dataSource))
                .jsonSerializer(jacksonSerializer)
                .build();
    }

    private static RdbmsSupport rdbmsSupport(DataSource dataSource) {
        String productName;
        try {
            productName = JdbcUtils.extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductName);
        } catch (MetaDataAccessException e) {
            throw new IllegalStateException("Cannot detect database product", e);
        }
        return switch (DatabaseDriver.fromProductName(productName)) {
            case H2 -> new H2RdbmsSupport();
            case POSTGRESQL -> new PostgreSqlRdbmsSupport();
            case MARIADB -> new MariaDbRdbmsSupport();
            case MYSQL -> new MySqlRdbmsSupport(new MySqlStoredProcedureSequenceGeneratorHandler("harbor_sequence_nextval"));
            default -> throw new IllegalStateException("Unsupported database: " + productName);
        };
    }
}
