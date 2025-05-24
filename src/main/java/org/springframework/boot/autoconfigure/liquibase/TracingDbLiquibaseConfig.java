package org.springframework.boot.autoconfigure.liquibase;

import com.hayden.tracingdb.config.TracingDbLiquibaseProperties;
import com.hayden.utilitymodule.db.DbDataSourceTrigger;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.UpdateSummaryEnum;
import liquibase.UpdateSummaryOutputEnum;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.ui.UIServiceEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@Profile("telemetrydb")
@Import(DbDataSourceTrigger.class)
public class TracingDbLiquibaseConfig {

    @Bean
    public LiquibaseSchemaManagementProvider liquibaseDefaultDdlModeProvider(
            ObjectProvider<SpringLiquibase> liquibases) {
        return new LiquibaseSchemaManagementProvider(liquibases);
    }

    public DataSource dataSource(LiquibaseProperties liquibaseProperties) {
        var ds = DataSourceBuilder
                .create()
                .url(liquibaseProperties.getUrl())
                .type(HikariDataSource.class)
                .username(liquibaseProperties.getUser())
                .password(liquibaseProperties.getPassword())
                .driverClassName(liquibaseProperties.getDriverClassName())
                .build();

        return ds;
    }


    @RequiredArgsConstructor
    public static class TracingDbRoutingDataSource extends AbstractRoutingDataSource {

        private final Map<String, DataSource> sources;
        private final DbDataSourceTrigger trigger;

        @Override
        protected Object determineCurrentLookupKey() {
            return trigger.currentKey();
        }
    }

    @Bean(name = {"tracingDbDataSource", "dataSource"})
    public DataSource dataSource(TracingDbLiquibaseProperties properties,
                                 DbDataSourceTrigger trigger) {
        trigger.setGlobalCurrentKey("main");
        Map<Object, Object> resolvedDataSources = new HashMap<>();
        Map<String, DataSource> exposedDataSources = new HashMap<>();
        TracingDbRoutingDataSource routingDataSource = new TracingDbRoutingDataSource(exposedDataSources, trigger);

        var eventDataSourceProp = properties.getLiquibase().get("event");
        var eventDataSource = dataSource(eventDataSourceProp);
        resolvedDataSources.put("event", eventDataSource);
        exposedDataSources.put("event", eventDataSource);

        routingDataSource.setDefaultTargetDataSource(eventDataSource);

        for (var e : properties.getLiquibase().entrySet()) {
            if (e.getKey().equals("event"))
                continue;
            DataSource dataSource = dataSource(e.getValue());
            resolvedDataSources.put(e.getKey(), dataSource);
            exposedDataSources.put(e.getKey(), dataSource);
        }

        routingDataSource.setTargetDataSources(resolvedDataSources);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("tracingDbDataSource") DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public CommandLineRunner liquibase(TracingDbLiquibaseProperties props,
                                       TracingDbRoutingDataSource tracingDbRoutingDataSource) {

        return args -> {
            for (var propItem : props.getLiquibase().entrySet()) {
                var properties = propItem.getValue();
                var connectionDetails = new LiquibaseAutoConfiguration.PropertiesLiquibaseConnectionDetails(properties);
                SpringLiquibase liquibase = createSpringLiquibase(tracingDbRoutingDataSource.sources.get(propItem.getKey()), connectionDetails);
                liquibase.setChangeLog(properties.getChangeLog());
                liquibase.setClearCheckSums(properties.isClearChecksums());
                liquibase.setContexts(String.join(", ", properties.getContexts()));
                liquibase.setDefaultSchema(properties.getDefaultSchema());
                liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
                liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
                liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
                liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
                liquibase.setDropFirst(properties.isDropFirst());
                liquibase.setShouldRun(true);
                liquibase.setLabelFilter(String.join(", ", properties.getLabelFilter()));
                liquibase.setChangeLogParameters(properties.getParameters());
                liquibase.setRollbackFile(properties.getRollbackFile());
                liquibase.setTestRollbackOnUpdate(properties.isTestRollbackOnUpdate());
                liquibase.setTag(properties.getTag());
                if (properties.getShowSummary() != null) {
                    liquibase.setShowSummary(UpdateSummaryEnum.valueOf(properties.getShowSummary().name()));
                }
                if (properties.getShowSummaryOutput() != null) {
                    liquibase
                            .setShowSummaryOutput(UpdateSummaryOutputEnum.valueOf(properties.getShowSummaryOutput().name()));
                }
                if (properties.getUiService() != null) {
                    liquibase.setUiService(UIServiceEnum.valueOf(properties.getUiService().name()));
                }
                liquibase.afterPropertiesSet();
            }
        };
    }

    private SpringLiquibase createSpringLiquibase(DataSource dataSource,
                                                  LiquibaseConnectionDetails connectionDetails) {
        DataSource migrationDataSource = getMigrationDataSource(dataSource, dataSource, connectionDetails);
        SpringLiquibase liquibase = (migrationDataSource == dataSource
                                     || migrationDataSource == dataSource) ? new SpringLiquibase()
                                                                           : new DataSourceClosingSpringLiquibase();
        liquibase.setDataSource(migrationDataSource);
        return liquibase;
    }

    private DataSource getMigrationDataSource(DataSource liquibaseDataSource, DataSource dataSource,
                                              LiquibaseConnectionDetails connectionDetails) {
        if (liquibaseDataSource != null) {
            return liquibaseDataSource;
        }
        String url = connectionDetails.getJdbcUrl();
        if (url != null) {
            DataSourceBuilder<?> builder = DataSourceBuilder.create().type(SimpleDriverDataSource.class);
            builder.url(url);
            applyConnectionDetails(connectionDetails, builder);
            return builder.build();
        }
        String user = connectionDetails.getUsername();
        if (user != null && dataSource != null) {
            DataSourceBuilder<?> builder = DataSourceBuilder.derivedFrom(dataSource)
                    .type(SimpleDriverDataSource.class);
            applyConnectionDetails(connectionDetails, builder);
            return builder.build();
        }
        Assert.state(dataSource != null, "Liquibase migration DataSource missing");
        return dataSource;
    }

    private void applyConnectionDetails(LiquibaseConnectionDetails connectionDetails,
                                        DataSourceBuilder<?> builder) {
        builder.username(connectionDetails.getUsername());
        builder.password(connectionDetails.getPassword());
        String driverClassName = connectionDetails.getDriverClassName();
        if (StringUtils.hasText(driverClassName)) {
            builder.driverClassName(driverClassName);
        }
    }

}
