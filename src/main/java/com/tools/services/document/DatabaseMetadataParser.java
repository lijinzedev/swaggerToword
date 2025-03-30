package com.tools.services.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.model.database.ColumnMetadata;
import com.tools.model.database.DatabaseMetadata;
import com.tools.model.database.IndexMetadata;
import com.tools.model.database.TableMetadata;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Utility class for parsing database metadata from JSON files
 */
public class DatabaseMetadataParser {

    private final ObjectMapper objectMapper;

    public DatabaseMetadataParser() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Parse JSON file into DatabaseMetadata object
     * @param jsonFile JSON file containing database metadata
     * @return DatabaseMetadata object
     * @throws IOException if file cannot be read or parsed
     */
    public DatabaseMetadata parseFromFile(File jsonFile) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        return parseJsonNode(rootNode);
    }

    /**
     * Parse JSON string into DatabaseMetadata object
     * @param jsonContent JSON string containing database metadata
     * @return DatabaseMetadata object
     * @throws IOException if string cannot be parsed
     */
    public DatabaseMetadata parseFromString(String jsonContent) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonContent);
        return parseJsonNode(rootNode);
    }

    /**
     * Parse JsonNode into DatabaseMetadata object
     * @param rootNode Root JSON node containing database metadata
     * @return DatabaseMetadata object
     */
    private DatabaseMetadata parseJsonNode(JsonNode rootNode) {
        DatabaseMetadata metadata = new DatabaseMetadata();
        
        // Parse basic database information
        metadata.setDatabaseName(getTextValue(rootNode, "databaseName"));
        metadata.setDatabaseType(getTextValue(rootNode, "databaseType"));
        metadata.setDatabaseVersion(getTextValue(rootNode, "databaseVersion"));
        
        // Parse tables
        JsonNode tablesNode = rootNode.get("tables");
        if (tablesNode != null && tablesNode.isArray()) {
            for (JsonNode tableNode : tablesNode) {
                TableMetadata table = parseTableMetadata(tableNode);
                metadata.addTable(table);
            }
        }
        
        return metadata;
    }
    
    /**
     * Parse table metadata from JSON node
     * @param tableNode JSON node containing table metadata
     * @return TableMetadata object
     */
    private TableMetadata parseTableMetadata(JsonNode tableNode) {
        TableMetadata table = new TableMetadata();
        
        // Parse basic table information
        table.setTableName(getTextValue(tableNode, "tableName"));
        table.setTableComment(getTextValue(tableNode, "tableComment"));
        table.setSchema(getTextValue(tableNode, "schema"));
        table.setTableSpace(getTextValue(tableNode, "tableSpace"));
        
        // Parse primary keys
        JsonNode primaryKeysNode = tableNode.get("primaryKeys");
        if (primaryKeysNode != null && primaryKeysNode.isArray()) {
            for (JsonNode keyNode : primaryKeysNode) {
                table.addPrimaryKey(keyNode.asText());
            }
        }
        
        // Parse logical keys
        JsonNode logicalKeysNode = tableNode.get("logicalKeys");
        if (logicalKeysNode != null && logicalKeysNode.isArray()) {
            for (JsonNode keyNode : logicalKeysNode) {
                table.addLogicalKey(keyNode.asText());
            }
        }
        
        // Parse indexes
        JsonNode indexesNode = tableNode.get("indexes");
        if (indexesNode != null && indexesNode.isArray()) {
            for (JsonNode indexNode : indexesNode) {
                IndexMetadata index = parseIndexMetadata(indexNode);
                table.addIndex(index);
            }
        }
        
        // Parse columns
        JsonNode columnsNode = tableNode.get("columns");
        if (columnsNode != null && columnsNode.isArray()) {
            for (JsonNode columnNode : columnsNode) {
                ColumnMetadata column = parseColumnMetadata(columnNode);
                table.addColumn(column);
            }
        }
        
        return table;
    }
    
    /**
     * Parse index metadata from JSON node
     * @param indexNode JSON node containing index metadata
     * @return IndexMetadata object
     */
    private IndexMetadata parseIndexMetadata(JsonNode indexNode) {
        IndexMetadata index = new IndexMetadata();
        
        index.setIndexName(getTextValue(indexNode, "indexName"));
        index.setUnique(getBooleanValue(indexNode, "isUnique", false));
        index.setIndexType(getTextValue(indexNode, "indexType"));
        index.setIndexComment(getTextValue(indexNode, "indexComment"));
        
        // Parse column names
        JsonNode columnNamesNode = indexNode.get("columnNames");
        if (columnNamesNode != null && columnNamesNode.isArray()) {
            for (JsonNode columnNameNode : columnNamesNode) {
                index.addColumnName(columnNameNode.asText());
            }
        }
        
        return index;
    }
    
    /**
     * Parse column metadata from JSON node
     * @param columnNode JSON node containing column metadata
     * @return ColumnMetadata object
     */
    private ColumnMetadata parseColumnMetadata(JsonNode columnNode) {
        ColumnMetadata column = new ColumnMetadata();
        
        column.setColumnName(getTextValue(columnNode, "columnName"));
        column.setColumnComment(getTextValue(columnNode, "columnComment"));
        column.setDataType(getTextValue(columnNode, "dataType"));
        column.setDefaultValue(getTextValue(columnNode, "defaultValue"));
        
        // Parse numeric values
        JsonNode columnSizeNode = columnNode.get("columnSize");
        if (columnSizeNode != null && !columnSizeNode.isNull()) {
            column.setColumnSize(columnSizeNode.asInt());
        }
        
        JsonNode decimalDigitsNode = columnNode.get("decimalDigits");
        if (decimalDigitsNode != null && !decimalDigitsNode.isNull()) {
            column.setDecimalDigits(decimalDigitsNode.asInt());
        }
        
        JsonNode ordinalPositionNode = columnNode.get("ordinalPosition");
        if (ordinalPositionNode != null && !ordinalPositionNode.isNull()) {
            column.setOrdinalPosition(ordinalPositionNode.asInt());
        }
        
        // Parse boolean values
        column.setPrimaryKey(getBooleanValue(columnNode, "isPrimaryKey", false));
        column.setNullable(getBooleanValue(columnNode, "isNullable", true));
        column.setForeignKey(getBooleanValue(columnNode, "isForeignKey", false));
        
        // Parse foreign key information
        if (column.isForeignKey()) {
            column.setForeignKeyTable(getTextValue(columnNode, "foreignKeyTable"));
            column.setForeignKeyColumn(getTextValue(columnNode, "foreignKeyColumn"));
        }
        
        return column;
    }
    
    /**
     * Get text value from JSON node
     * @param node JSON node
     * @param fieldName Field name
     * @return Text value or null if not found
     */
    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }
    
    /**
     * Get boolean value from JSON node
     * @param node JSON node
     * @param fieldName Field name
     * @param defaultValue Default value if field not found
     * @return Boolean value
     */
    private boolean getBooleanValue(JsonNode node, String fieldName, boolean defaultValue) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asBoolean() : defaultValue;
    }
} 