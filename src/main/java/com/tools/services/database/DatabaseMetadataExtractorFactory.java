package com.tools.services.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to get the appropriate database metadata extractor based on database type
 */
public class DatabaseMetadataExtractorFactory {
    
    private static final List<DatabaseMetadataExtractor> extractors = new ArrayList<>();
    
    static {
        // Register all extractors
        extractors.add(new MySqlMetadataExtractor());
        extractors.add(new PostgreSqlMetadataExtractor());
        extractors.add(new OracleMetadataExtractor());
        extractors.add(new SqlServerMetadataExtractor());
        extractors.add(new GenericMetadataExtractor()); // Fallback for any unsupported database
    }
    
    /**
     * Get the appropriate database metadata extractor for the specified database type
     * 
     * @param databaseType Type of database (mysql, oracle, postgresql, sqlserver, etc.)
     * @return The appropriate database metadata extractor
     */
    public static DatabaseMetadataExtractor getExtractor(String databaseType) {
        // Find the first extractor that supports the specified database type
        return extractors.stream()
                .filter(extractor -> extractor.supportsDatabase(databaseType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported database type: " + databaseType));
    }
} 