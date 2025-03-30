package com.tools.services.database;

import com.tools.model.database.DatabaseConnectionConfig;
import com.tools.model.database.DatabaseMetadata;

/**
 * 数据库元数据提取器接口
 * 定义从不同类型数据库中提取元数据的标准方法
 */
public interface DatabaseMetadataExtractor {
    
    /**
     * 从数据库中提取所有元数据
     * 
     * @param config 数据库连接配置
     * @return 完整的数据库元数据
     * @throws RuntimeException 如果提取过程中发生错误
     */
    DatabaseMetadata extractMetadata(DatabaseConnectionConfig config);
    
    /**
     * 提取特定表的元数据
     * 
     * @param config 数据库连接配置
     * @param tableName 要提取元数据的表名
     * @param schema 模式名称（可选，可为null）
     * @return 仅包含指定表的数据库元数据
     * @throws RuntimeException 如果提取过程中发生错误
     */
    DatabaseMetadata extractTableMetadata(DatabaseConnectionConfig config, String tableName, String schema);
    
    /**
     * 检查此提取器是否支持给定的数据库类型
     * 
     * @param databaseType 数据库类型字符串
     * @return 如果此提取器支持该数据库类型，则返回true
     */
    boolean supportsDatabase(String databaseType);
} 