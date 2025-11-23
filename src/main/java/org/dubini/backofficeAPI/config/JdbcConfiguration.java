package org.dubini.backofficeAPI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableJdbcRepositories(basePackages = "org.dubini.backofficeAPI.repository")
public class JdbcConfiguration extends AbstractJdbcConfiguration {

    @Bean
    public PGobjectToStringConverter pgobjectToStringConverter() {
        return new PGobjectToStringConverter();
    }

    @Bean
    public PostgresJsonbWritingConverter postgresJsonbWritingConverter() {
        return new PostgresJsonbWritingConverter();
    }

    @Override
    protected List<?> userConverters() {
        return Arrays.asList(
            pgobjectToStringConverter(),
            postgresJsonbWritingConverter()
        );
    }
}