package com.tools.model.database;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库表索引元数据
 * 描述表中索引的结构和属性
 */
@Data
@NoArgsConstructor
public class IndexMetadata {
    
    /**
     * 索引名称
     */
    private String indexName;
    
    /**
     * 是否为唯一索引
     */
    private boolean isUnique;
    
    /**
     * 索引包含的列名列表
     */
    private List<String> columnNames = new ArrayList<>();
    
    /**
     * 索引类型，如BTREE、HASH等
     */
    private String indexType;
    
    /**
     * 索引注释/说明
     */
    private String indexComment;
    
    /**
     * 添加列到索引
     *
     * @param columnName 列名
     */
    public void addColumnName(String columnName) {
        this.columnNames.add(columnName);
    }
    
    /**
     * 获取不可修改的列名列表
     *
     * @return 只读的索引列名列表
     */
    public List<String> getColumnNamesUnmodifiable() {
        return Collections.unmodifiableList(columnNames);
    }
    
    /**
     * 获取索引类型的描述文本
     *
     * @return 可读的索引类型描述
     */
    public String getIndexTypeDescription() {
        if (indexType == null || indexType.isEmpty()) {
            return "默认";
        }
        return indexType;
    }
    
    /**
     * 获取完整的索引描述，包括类型和唯一性
     *
     * @return 格式化的索引描述
     */
    public String getFullDescription() {
        StringBuilder description = new StringBuilder();
        
        if (isUnique) {
            description.append("唯一索引");
        } else {
            description.append("普通索引");
        }
        
        description.append(" (").append(getIndexTypeDescription()).append(")");
        description.append(" - 列: ").append(String.join(", ", columnNames));
        
        return description.toString();
    }
} 