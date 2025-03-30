package com.tools.model.database;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库表字段元数据
 * 描述数据库表中字段的详细信息
 */
@Data
@NoArgsConstructor
public class ColumnMetadata {
    
    /**
     * 字段序号，表示字段在表中的位置
     */
    private int ordinalPosition;
    
    /**
     * 字段名称
     */
    private String columnName;
    
    /**
     * 字段注释/说明
     */
    private String columnComment;
    
    /**
     * 数据类型，如VARCHAR、INT等
     */
    private String dataType;
    
    /**
     * 字段长度/大小
     */
    private Integer columnSize;
    
    /**
     * 小数位数（对于浮点类型）
     */
    private Integer decimalDigits;
    
    /**
     * 是否为主键的一部分
     */
    private boolean isPrimaryKey;
    
    /**
     * 是否允许为空
     */
    private boolean isNullable;
    
    /**
     * 是否为外键
     */
    private boolean isForeignKey;
    
    /**
     * 引用的外键表名（如果是外键）
     */
    private String foreignKeyTable;
    
    /**
     * 引用的外键列名（如果是外键）
     */
    private String foreignKeyColumn;
    
    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 获取完整的数据类型描述，包括长度和精度
     * 例如：VARCHAR(255)、DECIMAL(10,2)
     *
     * @return 格式化的数据类型字符串
     */
    public String getFormattedDataType() {
        StringBuilder dataTypeInfo = new StringBuilder(dataType);
        
        if (columnSize != null && columnSize > 0) {
            dataTypeInfo.append("(").append(columnSize);
            
            if (decimalDigits != null && decimalDigits > 0) {
                dataTypeInfo.append(",").append(decimalDigits);
            }
            
            dataTypeInfo.append(")");
        }
        
        return dataTypeInfo.toString();
    }
    
    /**
     * 获取字段的外键引用描述
     * 格式为：表名.列名
     *
     * @return 外键引用描述，如果不是外键则返回空字符串
     */
    public String getForeignKeyReference() {
        if (!isForeignKey || foreignKeyTable == null || foreignKeyColumn == null) {
            return "";
        }
        return foreignKeyTable + "." + foreignKeyColumn;
    }
} 