package com.hayden.tracingdb.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.liquibase.TracingDbLiquibaseConfig;
import org.springframework.context.annotation.*;
import org.springframework.data.jdbc.core.convert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hayden.tracingdb.entity.Event;
import com.hayden.tracingdb.repository.EventRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.conversion.MappingRelationalConverterImpl;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.PostgresDialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@EnableAutoConfiguration(exclude = {LiquibaseAutoConfiguration.class})
@EnableConfigurationProperties(LiquibaseProperties.class)
@AutoConfiguration
@EntityScan(basePackageClasses = {Event.class})
@ImportAutoConfiguration(value = {
        JdbcRepositoriesAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@Profile("telemetrydb")
@ComponentScan(basePackages = "com.hayden.tracingdb")
@EnableJdbcRepositories(basePackageClasses = {EventRepository.class})
@Import({FromPostgresJson.class, JdbcAnnotationConverter.class, JdbcTargetSqlTypesProvider.class,
         SqlParametersFactoryImpl.class, MappingRelationalConverterImpl.class, TracingDbLiquibaseConfig.class})
public class DatabaseConfiguration {

    @Bean
    @Primary
    public DataAccessStrategy dataAccessStrategy(NamedParameterJdbcOperations operations,
                                                 JdbcConverter jdbcConverter,
                                                 JdbcMappingContext context,
                                                 Dialect dialect) {
        var sqlGeneratorSource = new SqlGeneratorSource(context, jdbcConverter, dialect);
        var factory = new DataAccessStrategyFactory(
                sqlGeneratorSource,
                jdbcConverter,
                operations,
                new SqlParametersFactoryImpl(context, jdbcConverter),
                new InsertStrategyFactory(operations, dialect)
        );

        return factory.create();
    }

    @Bean
    public Dialect dialect() {
        return PostgresDialect.INSTANCE;
    }


    @Bean
    public NamedParameterJdbcOperations namedParameterJdbcOperations(JdbcTemplate jdbcTemplate){
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }


    @Bean
    JdbcMappingContext jdbcMappingContext() {
        return new JdbcMappingContext();
    }

    @Bean
    JdbcConverter jdbcConverter(RelationalMappingContext relationalMappingContext,
                                JdbcAnnotationConverter jdbcAnnotationConverter){
        return new MappingJdbcConverterImpl(relationalMappingContext, jdbcAnnotationConverter);
    }

    @Bean
    public ObjectMapper om(){
        var om = new ObjectMapper();
        om.registerModules(new JavaTimeModule());
        return om;
    }

}
