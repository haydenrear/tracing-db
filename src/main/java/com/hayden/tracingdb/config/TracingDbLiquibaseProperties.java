package com.hayden.tracingdb.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(prefix = "tracing-db")
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TracingDbLiquibaseProperties {

    Map<String, LiquibaseProperties> liquibase;

}
