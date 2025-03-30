package com.tools.services.database;

import com.tools.model.database.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * 数据库元数据提取器抽象基类
 * 提供通用的JDBC元数据提取实现，让子类专注于特定数据库的实现
 */
public abstract class AbstractDatabaseMetadataExtractor implements DatabaseMetadataExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractDatabaseMetadataExtractor.class);
    
    /**
     * 从数据库中提取所有元数据
     * 
     * @param config 数据库连接配置
     * @return 完整的数据库元数据
     * @throws RuntimeException 如果提取过程中发生严重错误
     */
    @Override
    public DatabaseMetadata extractMetadata(DatabaseConnectionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("数据库连接配置不能为空");
        }
        
        if (!config.isValid()) {
            throw new IllegalArgumentException("数据库连接配置无效，缺少必要参数");
        }
        
        DatabaseMetadata metadata = new DatabaseMetadata();
        metadata.setDatabaseName(config.getDatabaseName());
        metadata.setDatabaseType(config.getDatabaseType());
        metadata.setUsername(config.getUsername());
        metadata.setUrl(config.buildJdbcUrl());
        
        try (Connection connection = getConnection(config)) {
            DatabaseMetaData dbMetaData = connection.getMetaData();
            metadata.setDatabaseVersion(dbMetaData.getDatabaseProductVersion());
            
            String schema = config.getSchema();
            
            // 获取所有表
            try (ResultSet tables = getTables(dbMetaData, schema)) {
                while (tables.next()) {
                    try {
                        String tableName = tables.getString("TABLE_NAME");
                        String tableSchema = tables.getString("TABLE_SCHEM");
                        
                        logger.debug("正在提取表元数据: {}.{}", tableSchema, tableName);
                        TableMetadata tableMetadata = extractTableMetadata(dbMetaData, tableName, tableSchema);
                        if (tableMetadata != null) {
                            metadata.addTable(tableMetadata);
                        }
                    } catch (SQLException e) {
                        // 记录错误但继续处理下一个表
                        logger.warn("提取表元数据时发生错误: {}", e.getMessage());
                        if (logger.isDebugEnabled()) {
                            logger.debug("详细错误信息", e);
                        }
                    }
                }
            } catch (SQLException e) {
                // 记录错误但返回已提取的元数据
                logger.error("获取表列表时发生错误: {}", e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug("详细错误信息", e);
                }
            }
            
        } catch (SQLException e) {
            logger.error("连接数据库时发生错误: {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("详细错误信息", e);
            }
            throw new RuntimeException("提取数据库元数据失败", e);
        }
        
        return metadata;
    }
    
    /**
     * Extract metadata for a specific table
     */
    @Override
    public DatabaseMetadata extractTableMetadata(DatabaseConnectionConfig config, String tableName, String schema) {
        DatabaseMetadata metadata = new DatabaseMetadata();
        metadata.setDatabaseName(config.getDatabaseName());
        metadata.setDatabaseType(config.getDatabaseType());
        metadata.setUsername(config.getUsername());
        metadata.setUrl(config.buildJdbcUrl());
        
        Connection connection = null;
        try {
            connection = getConnection(config);
            DatabaseMetaData dbMetaData = connection.getMetaData();
            metadata.setDatabaseVersion(dbMetaData.getDatabaseProductVersion());
            
            // Get the specific table
            try {
                TableMetadata tableMetadata = extractTableMetadata(dbMetaData, tableName, schema);
                if (tableMetadata != null) {
                    metadata.addTable(tableMetadata);
                }
            } catch (SQLException e) {
                logger.error("Error extracting metadata for table {}: {}", tableName, e.getMessage());
                // Return metadata with empty tables list
            }
            
        } catch (SQLException e) {
            logger.error("Error connecting to database: {}", e.getMessage());
            throw new RuntimeException("Error extracting table metadata", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warn("Error closing database connection: {}", e.getMessage());
                }
            }
        }
        
        return metadata;
    }
    
    /**
     * Extract metadata for a table
     */
    protected TableMetadata extractTableMetadata(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException {
        TableMetadata tableMetadata = new TableMetadata();
        tableMetadata.setTableName(tableName);
        tableMetadata.setSchema(schema);
        
        // Get table comment
        try {
            tableMetadata.setTableComment(getTableComment(dbMetaData, tableName, schema));
        } catch (SQLException e) {
            logger.warn("Error getting table comment for table {}: {}", tableName, e.getMessage());
        }
        
        // Get table tablespace
        try {
            tableMetadata.setTableSpace(getTableSpace(dbMetaData, tableName, schema));
        } catch (SQLException e) {
            logger.warn("Error getting tablespace for table {}: {}", tableName, e.getMessage());
        }
        
        // Get primary keys
        try (ResultSet primaryKeys = dbMetaData.getPrimaryKeys(null, schema, tableName)) {
            while (primaryKeys.next()) {
                try {
                    String columnName = primaryKeys.getString("COLUMN_NAME");
                    tableMetadata.addPrimaryKey(columnName);
                } catch (SQLException e) {
                    // Log error but continue with next primary key
                    logger.warn("Error processing primary key information: {}", e.getMessage());
                }
            }
        } catch (SQLException e) {
            // Log error but continue with other metadata
            logger.warn("Error getting primary keys for table {}: {}", tableName, e.getMessage());
        }
        
        // Get indexes
        Map<String, IndexMetadata> indexMap = new HashMap<>();
        try (ResultSet indexes = dbMetaData.getIndexInfo(null, schema, tableName, false, false)) {
            while (indexes.next()) {
                try {
                    String indexName = indexes.getString("INDEX_NAME");
                    if (indexName == null) {
                        continue; // Skip if no index name
                    }
                    
                    IndexMetadata indexMetadata = indexMap.computeIfAbsent(indexName, k -> {
                        IndexMetadata idx = new IndexMetadata();
                        try {
                            idx.setIndexName(indexName);
                            idx.setUnique(!indexes.getBoolean("NON_UNIQUE"));
                            idx.setIndexType(indexes.getString("TYPE"));
                        } catch (SQLException e) {
                            // Log error but continue with available data
                            logger.warn("Error extracting index details for index {}: {}", indexName, e.getMessage());
                        }
                        return idx;
                    });
                    
                    indexMetadata.addColumnName(indexes.getString("COLUMN_NAME"));
                } catch (SQLException e) {
                    // Log error but continue with next index
                    logger.warn("Error processing index information: {}", e.getMessage());
                }
            }
        } catch (SQLException e) {
            // Log error but continue with other metadata
            logger.warn("Error getting indexes for table {}: {}", tableName, e.getMessage());
        }
        
        // Add all indexes to table metadata
        for (IndexMetadata index : indexMap.values()) {
            tableMetadata.addIndex(index);
        }
        
        // Get columns
        try (ResultSet columns = dbMetaData.getColumns(null, schema, tableName, null)) {
            while (columns.next()) {
                try {
                    ColumnMetadata columnMetadata = new ColumnMetadata();
                    
                    columnMetadata.setColumnName(columns.getString("COLUMN_NAME"));
                    columnMetadata.setDataType(columns.getString("TYPE_NAME"));
                    columnMetadata.setColumnSize(columns.getInt("COLUMN_SIZE"));
                    columnMetadata.setDecimalDigits(columns.getInt("DECIMAL_DIGITS"));
                    columnMetadata.setNullable("YES".equalsIgnoreCase(columns.getString("IS_NULLABLE")));
                    columnMetadata.setDefaultValue(columns.getString("COLUMN_DEF"));
                    columnMetadata.setOrdinalPosition(columns.getInt("ORDINAL_POSITION"));
                    columnMetadata.setColumnComment(columns.getString("REMARKS"));
                    
                    // Check if this column is a primary key
                    columnMetadata.setPrimaryKey(tableMetadata.getPrimaryKeys().contains(columnMetadata.getColumnName()));
                    
                    // Get extra column attributes specific to database type
                    try {
                        extractExtraColumnMetadata(dbMetaData, columnMetadata, tableName, schema);
                    } catch (SQLException e) {
                        // Log error but continue with standard metadata
                        logger.warn("Error extracting extra column metadata for column {}: {}", columnMetadata.getColumnName(), e.getMessage());
                    }
                    
                    tableMetadata.addColumn(columnMetadata);
                } catch (SQLException e) {
                    // Log error but continue with next column
                    logger.warn("Error processing column information: {}", e.getMessage());
                }
            }
        } catch (SQLException e) {
            // Log error but continue with other metadata
            logger.warn("Error getting columns for table {}: {}", tableName, e.getMessage());
        }
        
        // Get foreign keys
        try (ResultSet foreignKeys = dbMetaData.getImportedKeys(null, schema, tableName)) {
            while (foreignKeys.next()) {
                try {
                    String columnName = foreignKeys.getString("FKCOLUMN_NAME");
                    String refTableName = foreignKeys.getString("PKTABLE_NAME");
                    String refColumnName = foreignKeys.getString("PKCOLUMN_NAME");
                    
                    // Update column metadata for this foreign key
                    for (ColumnMetadata column : tableMetadata.getColumns()) {
                        if (column.getColumnName().equals(columnName)) {
                            column.setForeignKey(true);
                            column.setForeignKeyTable(refTableName);
                            column.setForeignKeyColumn(refColumnName);
                            break;
                        }
                    }
                } catch (SQLException e) {
                    // Log error but continue with next foreign key
                    logger.warn("Error processing foreign key information: {}", e.getMessage());
                }
            }
        } catch (SQLException e) {
            // Log error but continue with other metadata
            logger.warn("Error getting foreign keys for table {}: {}", tableName, e.getMessage());
        }
        
        return tableMetadata;
    }
    
    /**
     * Get a database connection
     */
    protected Connection getConnection(DatabaseConnectionConfig config) throws SQLException {
        String url = config.buildJdbcUrl();
        return DriverManager.getConnection(url, config.getUsername(), config.getPassword());
    }
    
    /**
     * Get result set of tables from database
     */
    protected ResultSet getTables(DatabaseMetaData dbMetaData, String schema) throws SQLException {
        return dbMetaData.getTables(null, schema, null, new String[]{"TABLE"});
    }
    
    /**
     * Get table comment (to be implemented by database-specific extractors)
     */
    protected abstract String getTableComment(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException;
    
    /**
     * Get table tablespace (to be implemented by database-specific extractors)
     */
    protected abstract String getTableSpace(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException;
    
    /**
     * Extract additional column metadata specific to database type
     */
    protected abstract void extractExtraColumnMetadata(DatabaseMetaData dbMetaData, ColumnMetadata columnMetadata, 
                                                      String tableName, String schema) throws SQLException;
} 