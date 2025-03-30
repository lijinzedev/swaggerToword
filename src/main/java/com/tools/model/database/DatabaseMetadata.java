package com.tools.model.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class representing database metadata including all tables and their details
 */
public class DatabaseMetadata {
    
    private String databaseName;
    private String databaseType;
    private String databaseVersion;
    private String url;
    private String username;
    private List<TableMetadata> tables;
    
    public DatabaseMetadata() {
        this.tables = new ArrayList<>();
    }
    
    public String getDatabaseName() {
        return databaseName;
    }
    
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    public String getDatabaseType() {
        return databaseType;
    }
    
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
    
    public String getDatabaseVersion() {
        return databaseVersion;
    }
    
    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public List<TableMetadata> getTables() {
        return tables;
    }
    
    public void setTables(List<TableMetadata> tables) {
        this.tables = tables;
    }
    
    public void addTable(TableMetadata table) {
        this.tables.add(table);
    }
} 