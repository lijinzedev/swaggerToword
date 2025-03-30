package com.tools.model.database;

/**
 * Entity class representing metadata for a table column
 */
public class ColumnMetadata {
    
    private int ordinalPosition;        // Column sequence number
    private String columnName;          // Physical column name 
    private String columnComment;       // Column comment/description
    private String dataType;            // Column data type
    private Integer columnSize;         // Column size/length
    private Integer decimalDigits;      // Decimal digits (precision)
    private boolean isPrimaryKey;       // Is part of primary key
    private boolean isNullable;         // Can be null
    private boolean isForeignKey;       // Is foreign key
    private String foreignKeyTable;     // Referenced table (if foreign key)
    private String foreignKeyColumn;    // Referenced column (if foreign key)
    private String defaultValue;        // Default value
    
    public ColumnMetadata() {
    }
    
    public int getOrdinalPosition() {
        return ordinalPosition;
    }
    
    public void setOrdinalPosition(int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }
    
    public String getColumnName() {
        return columnName;
    }
    
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    
    public String getColumnComment() {
        return columnComment;
    }
    
    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public Integer getColumnSize() {
        return columnSize;
    }
    
    public void setColumnSize(Integer columnSize) {
        this.columnSize = columnSize;
    }
    
    public Integer getDecimalDigits() {
        return decimalDigits;
    }
    
    public void setDecimalDigits(Integer decimalDigits) {
        this.decimalDigits = decimalDigits;
    }
    
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
    
    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }
    
    public boolean isNullable() {
        return isNullable;
    }
    
    public void setNullable(boolean nullable) {
        isNullable = nullable;
    }
    
    public boolean isForeignKey() {
        return isForeignKey;
    }
    
    public void setForeignKey(boolean foreignKey) {
        isForeignKey = foreignKey;
    }
    
    public String getForeignKeyTable() {
        return foreignKeyTable;
    }
    
    public void setForeignKeyTable(String foreignKeyTable) {
        this.foreignKeyTable = foreignKeyTable;
    }
    
    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }
    
    public void setForeignKeyColumn(String foreignKeyColumn) {
        this.foreignKeyColumn = foreignKeyColumn;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
} 