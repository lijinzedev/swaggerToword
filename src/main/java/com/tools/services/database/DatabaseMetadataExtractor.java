package com.tools.services.database;

import com.tools.model.database.DatabaseConnectionConfig;
import com.tools.model.database.DatabaseMetadata;

/**
 * Interface for extracting metadata from database
 */
public interface DatabaseMetadataExtractor {
    
    /**
     * Extract all metadata from database
     * 
     * @param config Database connection configuration
     * @return Complete database metadata
     */
    DatabaseMetadata extractMetadata(DatabaseConnectionConfig config);
    
    /**
     * Extract metadata for a specific table
     * 
     * @param config Database connection configuration
     * @param tableName Table name to extract metadata for
     * @param schema Schema name (optional, may be null)
     * @return Database metadata with only the specified table
     */
    DatabaseMetadata extractTableMetadata(DatabaseConnectionConfig config, String tableName, String schema);
    
    /**
     * Check if this extractor supports the given database type
     * 
     * @param databaseType Database type string
     * @return true if this extractor supports the database type
     */
    boolean supportsDatabase(String databaseType);
} 