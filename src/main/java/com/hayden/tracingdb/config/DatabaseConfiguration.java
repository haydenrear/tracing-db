package com.hayden.tracingdb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import javax.sql.DataSource;

@AutoConfiguration
@EntityScan(basePackageClasses = {Event.class})
@ImportAutoConfiguration(value = {
        JdbcRepositoriesAutoConfiguration.class,
                         JdbcTemplateAutoConfiguration.class,
                         DataSourceAutoConfiguration.class
})
@EnableConfigurationProperties
@Profile("telemetry-logging")
@EnableJdbcRepositories(basePackageClasses = {EventRepository.class})
public class DatabaseConfiguration {

    @Bean
    @Primary
    public DataAccessStrategy dataAccessStrategy(NamedParameterJdbcOperations operations,
                                          JdbcConverter jdbcConverter,
                                          JdbcMappingContext context,
                                          Dialect dialect) {
        var sqlGeneratorSource = new SqlGeneratorSource(context, jdbcConverter, dialect);
        var factory  = new DataAccessStrategyFactory(
                sqlGeneratorSource,
                jdbcConverter,
                operations,
                new SqlParametersFactoryImpl(context, jdbcConverter),
                new InsertStrategyFactory(operations, dialect)
        );

        return factory.create();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource(){
        return DataSourceBuilder
                .create()
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource){
        return new JdbcTemplate(dataSource);
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
