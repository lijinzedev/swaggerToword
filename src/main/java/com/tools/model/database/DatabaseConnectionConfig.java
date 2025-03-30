package com.tools.model.database;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库连接配置
 * 封装创建数据库连接所需的所有配置信息
 */
@Data
@NoArgsConstructor
public class DatabaseConnectionConfig {
    
    /**
     * 数据库类型（mysql、oracle、postgresql、sqlserver等）
     */
    private String databaseType;
    
    /**
     * 数据库主机地址
     */
    private String host;
    
    /**
     * 数据库端口
     */
    private int port;
    
    /**
     * 数据库/模式名称
     */
    private String databaseName;
    
    /**
     * 数据库用户名
     */
    private String username;
    
    /**
     * 数据库密码
     */
    private String password;
    
    /**
     * 模式名称（如适用）
     */
    private String schema;
    
    /**
     * 自定义JDBC URL（可选，可由其他属性构建）
     */
    private String jdbcUrl;
    
    /**
     * 根据数据库类型和其他属性构建JDBC URL
     * 
     * @return JDBC URL字符串
     * @throws IllegalArgumentException 如果数据库类型不支持
     */
    public String buildJdbcUrl() {
        if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
            return jdbcUrl;
        }
        
        String url;
        switch (getDatabaseTypeNormalized()) {
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
            case "mariadb":
                url = String.format("jdbc:mariadb://%s:%d/%s", host, port, databaseName);
                break;
            case "db2":
                url = String.format("jdbc:db2://%s:%d/%s", host, port, databaseName);
                break;
            default:
                throw new IllegalArgumentException("不支持的数据库类型: " + databaseType);
        }
        
        return url;
    }
    
    /**
     * 获取标准化的数据库类型（小写且去除空格）
     * 
     * @return 标准化后的数据库类型
     */
    private String getDatabaseTypeNormalized() {
        return databaseType != null ? databaseType.toLowerCase().trim() : "";
    }
    
    /**
     * 获取连接显示名称
     * 
     * @return 用于显示的连接名称
     */
    public String getDisplayName() {
        return String.format("%s@%s:%d/%s", 
                username, 
                host, 
                port, 
                databaseName);
    }
    
    /**
     * 验证配置是否完整
     * 
     * @return 如果配置完整则返回true
     */
    public boolean isValid() {
        // 如果提供了自定义JDBC URL，则只需要用户名和密码
        if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
            return username != null && !username.isEmpty();
        }
        
        // 否则需要所有基本连接信息
        return databaseType != null && !databaseType.isEmpty()
                && host != null && !host.isEmpty()
                && port > 0
                && databaseName != null && !databaseName.isEmpty()
                && username != null && !username.isEmpty();
    }
} 