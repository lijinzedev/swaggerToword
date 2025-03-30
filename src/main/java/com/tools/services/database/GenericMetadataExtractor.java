package com.tools.services.database;

import com.tools.model.database.ColumnMetadata;

import java.sql.*;

/**
 * Generic implementation of database metadata extractor
 * Used as a fallback for any database type that doesn't have a specific implementation
 */
public class GenericMetadataExtractor extends AbstractDatabaseMetadataExtractor {
    
    @Override
    public boolean supportsDatabase(String databaseType) {
        // This is a generic extractor, so it supports any database type
        // But it should be used as a last resort
        return true;
    }
    
    @Override
    protected String getTableComment(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException {
        // Use standard JDBC metadata to try to get table comment (REMARKS)
        try (ResultSet rs = dbMetaData.getTables(null, schema, tableName, null)) {
            if (rs.next()) {
                return rs.getString("REMARKS");
            }
        }
        return null;
    }
    
    @Override
    protected String getTableSpace(DatabaseMetaData dbMetaData, String tableName, String schema) 
            throws SQLException {
        // Generic implementation doesn't know how to get tablespace
        return null;
    }
    
    @Override
    protected void extractExtraColumnMetadata(DatabaseMetaData dbMetaData, ColumnMetadata columnMetadata, 
                                            String tableName, String schema) throws SQLException {
        // No additional column metadata to extract in the generic implementation
        // Standard metadata is already extracted in the abstract base class
    }
}