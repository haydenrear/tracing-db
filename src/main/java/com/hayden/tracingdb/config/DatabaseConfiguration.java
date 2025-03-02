package com.hayden.tracingdb.config;

import com.hayden.jdbc_persistence.config.JsonJdbcConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.liquibase.TracingDbLiquibaseConfig;
import org.springframework.context.annotation.*;

import com.hayden.tracingdb.entity.Event;
import com.hayden.tracingdb.repository.EventRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

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
@Import({TracingDbLiquibaseConfig.class, JsonJdbcConfig.class})
public class DatabaseConfiguration {


}
