package com.tools.model.database;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity class representing metadata for a table index
 */
public class IndexMetadata {
    
    private String indexName;            // Index name
    private boolean isUnique;            // Whether index is unique
    private List<String> columnNames;    // Columns in the index
    private String indexType;            // Type of index (e.g., BTREE, HASH, etc.)
    private String indexComment;         // Index comment/description
    
    public IndexMetadata() {
        this.columnNames = new ArrayList<>();
    }
    
    public String getIndexName() {
        return indexName;
    }
    
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    
    public boolean isUnique() {
        return isUnique;
    }
    
    public void setUnique(boolean unique) {
        isUnique = unique;
    }
    
    public List<String> getColumnNames() {
        return columnNames;
    }
    
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
    
    public void addColumnName(String columnName) {
        this.columnNames.add(columnName);
    }
    
    public String getIndexType() {
        return indexType;
    }
    
    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }
    
    public String getIndexComment() {
        return indexComment;
    }
    
    public void setIndexComment(String indexComment) {
        this.indexComment = indexComment;
    }
} 