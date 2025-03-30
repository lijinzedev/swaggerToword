package com.tools.model.database;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 数据库表元数据类
 * 描述数据库表的结构信息，包括列、主键、索引等
 */
@Data
@NoArgsConstructor
public class TableMetadata {
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 表注释/说明
     */
    private String tableComment;
    
    /**
     * 表空间名称
     */
    private String tableSpace;
    
    /**
     * 模式名称
     */
    private String schema;
    
    /**
     * 物理主键列表
     */
    private List<String> primaryKeys = new ArrayList<>();
    
    /**
     * 逻辑/业务键列表
     */
    private List<String> logicalKeys = new ArrayList<>();
    
    /**
     * 表索引列表
     */
    private List<IndexMetadata> indexes = new ArrayList<>();
    
    /**
     * 表字段列表
     */
    private List<ColumnMetadata> columns = new ArrayList<>();
    
    /**
     * 添加主键
     *
     * @param primaryKey 主键字段名
     */
    public void addPrimaryKey(String primaryKey) {
        this.primaryKeys.add(primaryKey);
    }
    
    /**
     * 添加逻辑/业务键
     *
     * @param logicalKey 逻辑键字段名
     */
    public void addLogicalKey(String logicalKey) {
        this.logicalKeys.add(logicalKey);
    }
    
    /**
     * 添加索引
     *
     * @param index 索引元数据
     */
    public void addIndex(IndexMetadata index) {
        this.indexes.add(index);
    }
    
    /**
     * 添加字段
     *
     * @param column 字段元数据
     */
    public void addColumn(ColumnMetadata column) {
        this.columns.add(column);
    }
    
    /**
     * 根据列名查找字段元数据
     *
     * @param columnName 列名
     * @return 可能包含字段元数据的Optional对象
     */
    public Optional<ColumnMetadata> findColumnByName(String columnName) {
        return columns.stream()
            .filter(col -> col.getColumnName().equalsIgnoreCase(columnName))
            .findFirst();
    }
    
    /**
     * 获取不可修改的主键列表
     *
     * @return 只读的主键列表
     */
    public List<String> getPrimaryKeysUnmodifiable() {
        return Collections.unmodifiableList(primaryKeys);
    }
    
    /**
     * 获取不可修改的索引列表
     *
     * @return 只读的索引列表
     */
    public List<IndexMetadata> getIndexesUnmodifiable() {
        return Collections.unmodifiableList(indexes);
    }
    
    /**
     * 获取不可修改的字段列表
     *
     * @return 只读的字段列表
     */
    public List<ColumnMetadata> getColumnsUnmodifiable() {
        return Collections.unmodifiableList(columns);
    }
} 