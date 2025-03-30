package com.tools.model.database;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * 数据库元数据信息
 * 包含数据库的基本信息以及所有表结构的详细元数据
 */
@Data
@NoArgsConstructor
public class DatabaseMetadata {
    
    /**
     * 数据库名称
     */
    private String databaseName;
    
    /**
     * 数据库类型（MySQL、Oracle、PostgreSQL等）
     */
    private String databaseType;
    
    /**
     * 数据库版本号
     */
    private String databaseVersion;
    
    /**
     * 数据库连接URL
     */
    private String url;
    
    /**
     * 数据库用户名
     */
    private String username;
    
    /**
     * 数据库中的表元数据列表
     */
    private List<TableMetadata> tables = new ArrayList<>();
    
    /**
     * 添加表元数据到数据库元数据中
     * 
     * @param table 表元数据对象
     */
    public void addTable(TableMetadata table) {
        this.tables.add(table);
    }
    
    /**
     * 获取数据库中所有表的只读列表
     * 
     * @return 只读的表元数据列表
     */
    public List<TableMetadata> getTablesUnmodifiable() {
        return Collections.unmodifiableList(tables);
    }
} 