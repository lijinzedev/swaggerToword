package com.tools.services.database;

import com.tools.model.database.ColumnMetadata;

import java.sql.*;

/**
 * SQL Server specific implementation of database metadata extractor
 */
public class SqlServerMetadataExtractor extends AbstractDatabaseMetadataExtractor {
    
    private static final String TABLE_COMMENT_QUERY = 
            "SELECT ep.value AS table_comment, t.name AS table_name, s.name AS filegroup_name " +
            "FROM sys.tables t " +
            "LEFT JOIN sys.extended_properties ep ON ep.major_id = t.object_id AND ep.minor_id = 0 AND ep.name = 'MS_Description' " +
            "LEFT JOIN sys.filegroups s ON t.lob_data_space_id = s.data_space_id " +
            "WHERE SCHEMA_NAME(t.schema_id) = ? AND t.name = ?";
    
    private static final String COLUMN_COMMENT_QUERY = 
            "SELECT ep.value AS column_comment " +
            "FROM sys.columns c " +
            "INNER JOIN sys.tables t ON c.object_id = t.object_id " +
            "LEFT JOIN sys.extended_properties ep ON ep.major_id = t.object_id AND ep.minor_id = c.column_id AND ep.name = 'MS_Description' " +
            "WHERE SCHEMA_NAME(t.schema_id) = ? AND t.name = ? AND c.name = ?";
    
    @Override
    public boolean supportsDatabase(String databaseType) {
        return "sqlserver".equalsIgnoreCase(databaseType) || "mssql".equalsIgnoreCase(databaseType);
    }
    
    @Override
    protected ResultSet getTables(DatabaseMetaData dbMetaData, String schema) throws SQLException {
        // In SQL Server, if schema is null, we use "dbo" schema by default
        return dbMetaData.getTables(null, schema != null ? schema : "dbo", null, new String[]{"TABLE"});
    }
    
    @Override
    protected String getTableComment(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException {
        Connection connection = dbMetaData.getConnection();
        
        // In SQL Server, if schema is null, we use "dbo" schema by default
        if (schema == null || schema.isEmpty()) {
            schema = "dbo";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(TABLE_COMMENT_QUERY)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("table_comment");
                }
            }
        }
        
        return null;
    }
    
    @Override
    protected String getTableSpace(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException {
        Connection connection = dbMetaData.getConnection();
        
        // In SQL Server, if schema is null, we use "dbo" schema by default
        if (schema == null || schema.isEmpty()) {
            schema = "dbo";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(TABLE_COMMENT_QUERY)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("filegroup_name");
                }
            }
        }
        
        return null;
    }
    
    @Override
    protected void extractExtraColumnMetadata(DatabaseMetaData dbMetaData, ColumnMetadata columnMetadata, 
                                            String tableName, String schema) throws SQLException {
        Connection connection = dbMetaData.getConnection();
        
        // In SQL Server, if schema is null, we use "dbo" schema by default
        if (schema == null || schema.isEmpty()) {
            schema = "dbo";
        }
        
        // Get column comment from SQL Server system tables
        try (PreparedStatement stmt = connection.prepareStatement(COLUMN_COMMENT_QUERY)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            stmt.setString(3, columnMetadata.getColumnName());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String comment = rs.getString("column_comment");
                    if (comment != null && !comment.isEmpty()) {
                        columnMetadata.setColumnComment(comment);
                    }
                }
            }
        }
        
        // Get column default and nullable from SQL Server system tables
        String query = "SELECT c.name AS column_name, " +
                "t.name AS data_type, " +
                "c.max_length, " +
                "c.precision, " +
                "c.scale, " +
                "c.is_nullable, " +
                "d.definition AS column_default " +
                "FROM sys.columns c " +
                "INNER JOIN sys.tables tab ON tab.object_id = c.object_id " +
                "INNER JOIN sys.types t ON c.user_type_id = t.user_type_id " +
                "LEFT JOIN sys.default_constraints d ON c.default_object_id = d.object_id " +
                "WHERE SCHEMA_NAME(tab.schema_id) = ? AND tab.name = ? AND c.name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            stmt.setString(3, columnMetadata.getColumnName());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    columnMetadata.setDefaultValue(rs.getString("column_default"));
                    columnMetadata.setNullable(rs.getBoolean("is_nullable"));
                }
            }
        }
    }
} 