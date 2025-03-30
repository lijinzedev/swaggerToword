package com.tools.model.database;

/**
 * Configuration class for database connections
 */
public class DatabaseConnectionConfig {
    
    private String databaseType;  // Type of database (mysql, oracle, postgresql, sqlserver, etc.)
    private String host;          // Database host
    private int port;             // Database port
    private String databaseName;  // Database/schema name
    private String username;      // Database username
    private String password;      // Database password
    private String schema;        // Schema name (if applicable)
    private String jdbcUrl;       // Custom JDBC URL (optional, can be built from other properties)
    
    public DatabaseConnectionConfig() {
    }
    
    public String getDatabaseType() {
        return databaseType;
    }
    
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getDatabaseName() {
        return databaseName;
    }
    
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public String getJdbcUrl() {
        return jdbcUrl;
    }
    
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }
    
    /**
     * Build JDBC URL based on database type and other properties
     * @return JDBC URL string
     */
    public String buildJdbcUrl() {
        if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
            return jdbcUrl;
        }
        
        String url;
        switch (databaseType.toLowerCase()) {
            case "mysql":
                url = String.format("jdbc:mysql://%s:%d/%s", host, port, databaseName);
                break;
            case "postgresql":
                url = String.format("jdbc:postgresql://%s:%d/%s", host, port, databaseName);
                break;
            case "oracle":
                url = String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, databaseName);
                break;
            case "sqlserver":
                url = String.format("jdbc:sqlserver://%s:%d;databaseName=%s", host, port, databaseName);
                break;
            case "h2":
                url = String.format("jdbc:h2:mem:%s", databaseName);
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
        
        return url;
    }
} 