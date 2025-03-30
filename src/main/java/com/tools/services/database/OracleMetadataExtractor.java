package com.tools.services.database;

import com.tools.model.database.ColumnMetadata;

import java.sql.*;

/**
 * Oracle specific implementation of database metadata extractor
 */
public class OracleMetadataExtractor extends AbstractDatabaseMetadataExtractor {
    
    private static final String TABLE_COMMENT_QUERY = 
            "SELECT comments FROM all_tab_comments " +
            "WHERE owner = ? AND table_name = ?";
    
    private static final String TABLE_SPACE_QUERY = 
            "SELECT tablespace_name FROM all_tables " +
            "WHERE owner = ? AND table_name = ?";
    
    private static final String COLUMN_COMMENT_QUERY = 
            "SELECT comments FROM all_col_comments " +
            "WHERE owner = ? AND table_name = ? AND column_name = ?";
    
    @Override
    public boolean supportsDatabase(String databaseType) {
        return "oracle".equalsIgnoreCase(databaseType);
    }
    
    @Override
    protected ResultSet getTables(DatabaseMetaData dbMetaData, String schema) throws SQLException {
        // In Oracle, if schema is null, we use the current user's schema
        if (schema == null || schema.isEmpty()) {
            try (Statement stmt = dbMetaData.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL")) {
                if (rs.next()) {
                    schema = rs.getString(1);
                }
            }
        }
        
        // In Oracle, schema is the owner
        return dbMetaData.getTables(null, schema.toUpperCase(), null, new String[]{"TABLE"});
    }
    
    @Override
    protected String getTableComment(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException {
        Connection connection = dbMetaData.getConnection();
        
        // In Oracle, if schema is null, we use the current user's schema
        if (schema == null || schema.isEmpty()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL")) {
                if (rs.next()) {
                    schema = rs.getString(1);
                }
            }
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(TABLE_COMMENT_QUERY)) {
            stmt.setString(1, schema.toUpperCase());
            stmt.setString(2, tableName.toUpperCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("comments");
                }
            }
        }
        
        return null;
    }
    
    @Override
    protected String getTableSpace(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException {
        Connection connection = dbMetaData.getConnection();
        
        // In Oracle, if schema is null, we use the current user's schema
        if (schema == null || schema.isEmpty()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL")) {
                if (rs.next()) {
                    schema = rs.getString(1);
                }
            }
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(TABLE_SPACE_QUERY)) {
            stmt.setString(1, schema.toUpperCase());
            stmt.setString(2, tableName.toUpperCase());
            
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
        
        // In Oracle, if schema is null, we use the current user's schema
        if (schema == null || schema.isEmpty()) {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL")) {
                if (rs.next()) {
                    schema = rs.getString(1);
                }
            }
        }
        
        // Get column comment from Oracle system tables
        try (PreparedStatement stmt = connection.prepareStatement(COLUMN_COMMENT_QUERY)) {
            stmt.setString(1, schema.toUpperCase());
            stmt.setString(2, tableName.toUpperCase());
            stmt.setString(3, columnMetadata.getColumnName().toUpperCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String comment = rs.getString("comments");
                    if (comment != null && !comment.isEmpty()) {
                        columnMetadata.setColumnComment(comment);
                    }
                }
            }
        }
        
        // Get column additional info from Oracle system tables
        String query = "SELECT data_default, nullable " +
                "FROM all_tab_columns " +
                "WHERE owner = ? AND table_name = ? AND column_name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema.toUpperCase());
            stmt.setString(2, tableName.toUpperCase());
            stmt.setString(3, columnMetadata.getColumnName().toUpperCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    columnMetadata.setDefaultValue(rs.getString("data_default"));
                    columnMetadata.setNullable("Y".equalsIgnoreCase(rs.getString("nullable")));
                }
            }
        }
    }
} 