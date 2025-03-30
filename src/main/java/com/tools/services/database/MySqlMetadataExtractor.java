package com.tools.services.database;

import com.tools.model.database.ColumnMetadata;

import java.sql.*;

/**
 * MySQL specific implementation of database metadata extractor
 */
public class MySqlMetadataExtractor extends AbstractDatabaseMetadataExtractor {
    
    private static final String TABLE_COMMENT_QUERY = 
            "SELECT table_comment, tablespace_name FROM information_schema.tables " +
            "WHERE table_schema = ? AND table_name = ?";
    
    @Override
    public boolean supportsDatabase(String databaseType) {
        return "mysql".equalsIgnoreCase(databaseType);
    }
    
    @Override
    protected String getTableComment(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException {
        Connection connection = dbMetaData.getConnection();
        
        // If schema is not provided, use the current database
        if (schema == null || schema.isEmpty()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    schema = rs.getString(1);
                }
            }
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
        
        // If schema is not provided, use the current database
        if (schema == null || schema.isEmpty()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    schema = rs.getString(1);
                }
            }
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(TABLE_COMMENT_QUERY)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tablespace_name");
                }
            }
        }
        
        return null;
    }
    
    @Override
    protected void extractExtraColumnMetadata(DatabaseMetaData dbMetaData, ColumnMetadata columnMetadata, 
                                            String tableName, String schema) throws SQLException {
        Connection connection = dbMetaData.getConnection();
        
        // If schema is not provided, use the current database
        if (schema == null || schema.isEmpty()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    schema = rs.getString(1);
                }
            }
        }
        
        // In MySQL, we can get additional column information from information_schema.columns
        String query = "SELECT COLUMN_DEFAULT, IS_NULLABLE, COLUMN_COMMENT " +
                "FROM information_schema.columns " +
                "WHERE table_schema = ? AND table_name = ? AND column_name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            stmt.setString(3, columnMetadata.getColumnName());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    columnMetadata.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
                    columnMetadata.setNullable("YES".equalsIgnoreCase(rs.getString("IS_NULLABLE")));
                    
                    // Get column comment if not already set
                    if (columnMetadata.getColumnComment() == null || columnMetadata.getColumnComment().isEmpty()) {
                        columnMetadata.setColumnComment(rs.getString("COLUMN_COMMENT"));
                    }
                }
            }
        }
    }
} 