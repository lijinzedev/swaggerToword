package com.tools.model.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing metadata for a database table
 */
public class TableMetadata {
    
    private String tableName;           // Physical table name
    private String tableComment;        // Table comment/description
    private String tableSpace;          // Tablespace name
    private String schema;              // Schema name
    private List<String> primaryKeys;   // Physical primary keys
    private List<String> logicalKeys;   // Logical/business keys  
    private List<IndexMetadata> indexes; // Table indexes
    private List<ColumnMetadata> columns; // Table columns
    
    public TableMetadata() {
        this.primaryKeys = new ArrayList<>();
        this.logicalKeys = new ArrayList<>();
        this.indexes = new ArrayList<>();
        this.columns = new ArrayList<>();
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getTableComment() {
        return tableComment;
    }
    
    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }
    
    public String getTableSpace() {
        return tableSpace;
    }
    
    public void setTableSpace(String tableSpace) {
        this.tableSpace = tableSpace;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }
    
    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }
    
    public void addPrimaryKey(String primaryKey) {
        this.primaryKeys.add(primaryKey);
    }
    
    public List<String> getLogicalKeys() {
        return logicalKeys;
    }
    
    public void setLogicalKeys(List<String> logicalKeys) {
        this.logicalKeys = logicalKeys;
    }
    
    public void addLogicalKey(String logicalKey) {
        this.logicalKeys.add(logicalKey);
    }
    
    public List<IndexMetadata> getIndexes() {
        return indexes;
    }
    
    public void setIndexes(List<IndexMetadata> indexes) {
        this.indexes = indexes;
    }
    
    public void addIndex(IndexMetadata index) {
        this.indexes.add(index);
    }
    
    public List<ColumnMetadata> getColumns() {
        return columns;
    }
    
    public void setColumns(List<ColumnMetadata> columns) {
        this.columns = columns;
    }
    
    public void addColumn(ColumnMetadata column) {
        this.columns.add(column);
    }
} 