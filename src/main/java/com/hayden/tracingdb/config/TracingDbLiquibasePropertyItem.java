package com.hayden.tracingdb.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

import java.io.File;
import java.util.Map;

public class TracingDbLiquibasePropertyItem extends LiquibaseProperties {

    @Override
    public String getChangeLog() {
        return super.getChangeLog();
    }

    @Override
    public void setChangeLog(String changeLog) {
        super.setChangeLog(changeLog);
    }

    @Override
    public String getContexts() {
        return super.getContexts();
    }

    @Override
    public void setContexts(String contexts) {
        super.setContexts(contexts);
    }

    @Override
    public String getDefaultSchema() {
        return super.getDefaultSchema();
    }

    @Override
    public void setDefaultSchema(String defaultSchema) {
        super.setDefaultSchema(defaultSchema);
    }

    @Override
    public String getLiquibaseSchema() {
        return super.getLiquibaseSchema();
    }

    @Override
    public void setLiquibaseSchema(String liquibaseSchema) {
        super.setLiquibaseSchema(liquibaseSchema);
    }

    @Override
    public String getLiquibaseTablespace() {
        return super.getLiquibaseTablespace();
    }

    @Override
    public void setLiquibaseTablespace(String liquibaseTablespace) {
        super.setLiquibaseTablespace(liquibaseTablespace);
    }

    @Override
    public String getDatabaseChangeLogTable() {
        return super.getDatabaseChangeLogTable();
    }

    @Override
    public void setDatabaseChangeLogTable(String databaseChangeLogTable) {
        super.setDatabaseChangeLogTable(databaseChangeLogTable);
    }

    @Override
    public String getDatabaseChangeLogLockTable() {
        return super.getDatabaseChangeLogLockTable();
    }

    @Override
    public void setDatabaseChangeLogLockTable(String databaseChangeLogLockTable) {
        super.setDatabaseChangeLogLockTable(databaseChangeLogLockTable);
    }

    @Override
    public boolean isDropFirst() {
        return super.isDropFirst();
    }

    @Override
    public void setDropFirst(boolean dropFirst) {
        super.setDropFirst(dropFirst);
    }

    @Override
    public boolean isClearChecksums() {
        return super.isClearChecksums();
    }

    @Override
    public void setClearChecksums(boolean clearChecksums) {
        super.setClearChecksums(clearChecksums);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    public String getUser() {
        return super.getUser();
    }

    @Override
    public void setUser(String user) {
        super.setUser(user);
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(password);
    }

    @Override
    public String getDriverClassName() {
        return super.getDriverClassName();
    }

    @Override
    public void setDriverClassName(String driverClassName) {
        super.setDriverClassName(driverClassName);
    }

    @Override
    public String getUrl() {
        return super.getUrl();
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(url);
    }

    @Override
    public String getLabelFilter() {
        return super.getLabelFilter();
    }

    @Override
    public void setLabelFilter(String labelFilter) {
        super.setLabelFilter(labelFilter);
    }

    @Override
    public Map<String, String> getParameters() {
        return super.getParameters();
    }

    @Override
    public void setParameters(Map<String, String> parameters) {
        super.setParameters(parameters);
    }

    @Override
    public File getRollbackFile() {
        return super.getRollbackFile();
    }

    @Override
    public void setRollbackFile(File rollbackFile) {
        super.setRollbackFile(rollbackFile);
    }

    @Override
    public boolean isTestRollbackOnUpdate() {
        return super.isTestRollbackOnUpdate();
    }

    @Override
    public void setTestRollbackOnUpdate(boolean testRollbackOnUpdate) {
        super.setTestRollbackOnUpdate(testRollbackOnUpdate);
    }

    @Override
    public String getTag() {
        return super.getTag();
    }

    @Override
    public void setTag(String tag) {
        super.setTag(tag);
    }

    @Override
    public ShowSummary getShowSummary() {
        return super.getShowSummary();
    }

    @Override
    public void setShowSummary(ShowSummary showSummary) {
        super.setShowSummary(showSummary);
    }

    @Override
    public ShowSummaryOutput getShowSummaryOutput() {
        return super.getShowSummaryOutput();
    }

    @Override
    public void setShowSummaryOutput(ShowSummaryOutput showSummaryOutput) {
        super.setShowSummaryOutput(showSummaryOutput);
    }

    @Override
    public UiService getUiService() {
        return super.getUiService();
    }

    @Override
    public void setUiService(UiService uiService) {
        super.setUiService(uiService);
    }
}
