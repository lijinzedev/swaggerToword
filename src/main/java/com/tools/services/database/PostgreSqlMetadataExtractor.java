package com.tools.services.database;

import com.tools.model.database.ColumnMetadata;

import java.sql.*;

/**
 * PostgreSQL specific implementation of database metadata extractor
 */
public class PostgreSqlMetadataExtractor extends AbstractDatabaseMetadataExtractor {
    
    private static final String TABLE_COMMENT_QUERY = 
            "SELECT obj_description(c.oid) AS table_comment, t.spcname AS tablespace_name " +
            "FROM pg_class c " +
            "LEFT JOIN pg_tablespace t ON c.reltablespace = t.oid " +
            "WHERE c.relname = ? AND c.relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = ?)";
    
    private static final String COLUMN_COMMENT_QUERY = 
            "SELECT col_description(a.attrelid, a.attnum) AS column_comment " +
            "FROM pg_catalog.pg_attribute a " +
            "JOIN pg_catalog.pg_class c ON a.attrelid = c.oid " +
            "JOIN pg_catalog.pg_namespace n ON c.relnamespace = n.oid " +
            "WHERE c.relname = ? AND n.nspname = ? AND a.attname = ?";
    
    @Override
    public boolean supportsDatabase(String databaseType) {
        return "postgresql".equalsIgnoreCase(databaseType);
    }
    
    @Override
    protected ResultSet getTables(DatabaseMetaData dbMetaData, String schema) throws SQLException {
        // In PostgreSQL, if schema is null, we use "public" schema by default
        return dbMetaData.getTables(null, schema != null ? schema : "public", null, new String[]{"TABLE"});
    }
    
    @Override
    protected String getTableComment(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException {
        Connection connection = dbMetaData.getConnection();
        
        // In PostgreSQL, if schema is null, we use "public" schema by default
        if (schema == null || schema.isEmpty()) {
            schema = "public";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(TABLE_COMMENT_QUERY)) {
            stmt.setString(1, tableName);
            stmt.setString(2, schema);
            
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
        
        // In PostgreSQL, if schema is null, we use "public" schema by default
        if (schema == null || schema.isEmpty()) {
            schema = "public";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(TABLE_COMMENT_QUERY)) {
            stmt.setString(1, tableName);
            stmt.setString(2, schema);
            
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
        
        // In PostgreSQL, if schema is null, we use "public" schema by default
        if (schema == null || schema.isEmpty()) {
            schema = "public";
        }
        
        // Get column comment from PostgreSQL system tables
        try (PreparedStatement stmt = connection.prepareStatement(COLUMN_COMMENT_QUERY)) {
            stmt.setString(1, tableName);
            stmt.setString(2, schema);
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
        
        // Get column default, nullable from PostgreSQL information_schema
        String query = "SELECT column_default, is_nullable " +
                "FROM information_schema.columns " +
                "WHERE table_schema = ? AND table_name = ? AND column_name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            stmt.setString(3, columnMetadata.getColumnName());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    columnMetadata.setDefaultValue(rs.getString("column_default"));
                    columnMetadata.setNullable("YES".equalsIgnoreCase(rs.getString("is_nullable")));
                }
            }
        }
    }
}