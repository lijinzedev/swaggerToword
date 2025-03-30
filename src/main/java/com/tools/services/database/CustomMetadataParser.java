package com.tools.services.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.model.database.ColumnMetadata;
import com.tools.model.database.DatabaseMetadata;
import com.tools.model.database.IndexMetadata;
import com.tools.model.database.TableMetadata;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service for parsing custom uploaded metadata files
 */
@Service
public class CustomMetadataParser {
    
    private final ObjectMapper objectMapper;
    
    public CustomMetadataParser() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Parse JSON format custom metadata
     * 
     * @param inputStream Input stream with JSON data
     * @return Database metadata object
     * @throws IOException If parsing fails
     */
    public DatabaseMetadata parseJsonMetadata(InputStream inputStream) throws IOException {
        Map<String, Object> jsonData = objectMapper.readValue(inputStream, Map.class);
        
        DatabaseMetadata metadata = new DatabaseMetadata();
        
        // Parse database information
        metadata.setDatabaseName((String) jsonData.getOrDefault("databaseName", "Custom Database"));
        metadata.setDatabaseType((String) jsonData.getOrDefault("databaseType", "Custom"));
        metadata.setDatabaseVersion((String) jsonData.getOrDefault("databaseVersion", "1.0"));
        
        // Parse tables
        List<Map<String, Object>> tables = (List<Map<String, Object>>) jsonData.get("tables");
        if (tables != null) {
            for (Map<String, Object> tableData : tables) {
                TableMetadata table = parseTableMetadata(tableData);
                metadata.addTable(table);
            }
        }
        
        return metadata;
    }
    
    /**
     * Parse table metadata from a JSON object
     * 
     * @param tableData Table data as a map
     * @return Table metadata object
     */
    private TableMetadata parseTableMetadata(Map<String, Object> tableData) {
        TableMetadata table = new TableMetadata();
        
        // Basic table info
        table.setTableName((String) tableData.get("tableName"));
        table.setTableComment((String) tableData.get("tableComment"));
        table.setTableSpace((String) tableData.get("tableSpace"));
        table.setSchema((String) tableData.get("schema"));
        
        // Primary keys
        List<String> primaryKeys = (List<String>) tableData.get("primaryKeys");
        if (primaryKeys != null) {
            for (String pk : primaryKeys) {
                table.addPrimaryKey(pk);
            }
        }
        
        // Logical keys
        List<String> logicalKeys = (List<String>) tableData.get("logicalKeys");
        if (logicalKeys != null) {
            for (String lk : logicalKeys) {
                table.addLogicalKey(lk);
            }
        }
        
        // Indexes
        List<Map<String, Object>> indexes = (List<Map<String, Object>>) tableData.get("indexes");
        if (indexes != null) {
            for (Map<String, Object> indexData : indexes) {
                IndexMetadata index = parseIndexMetadata(indexData);
                table.addIndex(index);
            }
        }
        
        // Columns
        List<Map<String, Object>> columns = (List<Map<String, Object>>) tableData.get("columns");
        if (columns != null) {
            for (Map<String, Object> columnData : columns) {
                ColumnMetadata column = parseColumnMetadata(columnData);
                table.addColumn(column);
            }
        }
        
        return table;
    }
    
    /**
     * Parse index metadata from a JSON object
     * 
     * @param indexData Index data as a map
     * @return Index metadata object
     */
    private IndexMetadata parseIndexMetadata(Map<String, Object> indexData) {
        IndexMetadata index = new IndexMetadata();
        
        index.setIndexName((String) indexData.get("indexName"));
        index.setUnique((Boolean) indexData.getOrDefault("isUnique", false));
        index.setIndexType((String) indexData.get("indexType"));
        index.setIndexComment((String) indexData.get("indexComment"));
        
        List<String> columnNames = (List<String>) indexData.get("columnNames");
        if (columnNames != null) {
            for (String columnName : columnNames) {
                index.addColumnName(columnName);
            }
        }
        
        return index;
    }
    
    /**
     * Parse column metadata from a JSON object
     * 
     * @param columnData Column data as a map
     * @return Column metadata object
     */
    private ColumnMetadata parseColumnMetadata(Map<String, Object> columnData) {
        ColumnMetadata column = new ColumnMetadata();
        
        column.setColumnName((String) columnData.get("columnName"));
        column.setColumnComment((String) columnData.get("columnComment"));
        column.setDataType((String) columnData.get("dataType"));
        
        if (columnData.get("columnSize") != null) {
            column.setColumnSize(((Number) columnData.get("columnSize")).intValue());
        }
        
        if (columnData.get("decimalDigits") != null) {
            column.setDecimalDigits(((Number) columnData.get("decimalDigits")).intValue());
        }
        
        column.setPrimaryKey((Boolean) columnData.getOrDefault("isPrimaryKey", false));
        column.setNullable((Boolean) columnData.getOrDefault("isNullable", true));
        column.setForeignKey((Boolean) columnData.getOrDefault("isForeignKey", false));
        column.setForeignKeyTable((String) columnData.get("foreignKeyTable"));
        column.setForeignKeyColumn((String) columnData.get("foreignKeyColumn"));
        column.setDefaultValue((String) columnData.get("defaultValue"));
        
        if (columnData.get("ordinalPosition") != null) {
            column.setOrdinalPosition(((Number) columnData.get("ordinalPosition")).intValue());
        } else {
            column.setOrdinalPosition(0);
        }
        
        return column;
    }
} 