package com.tools.services.document;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.*;
import com.deepoove.poi.policy.DynamicTableRenderPolicy;
import com.deepoove.poi.policy.TableRenderPolicy;
import com.deepoove.poi.util.TableTools;
import com.tools.model.database.ColumnMetadata;
import com.tools.model.database.DatabaseMetadata;
import com.tools.model.database.IndexMetadata;
import com.tools.model.database.TableMetadata;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.core.io.ClassPathResource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Service for generating Word documents from database metadata
 */
public class DatabaseDocumentService {
    
    /**
     * Generate a Word document from database metadata
     * 
     * @param metadata Database metadata
     * @param templatePath Path to the Word template
     * @param outputPath Path for the generated document
     * @throws IOException If an error occurs during document generation
     */
    public void generateDocument(DatabaseMetadata metadata, String templatePath, String outputPath) throws IOException {
        // Prepare data for the template
        Map<String, Object> data = prepareTemplateData(metadata);
        
        // Configure and render the template with custom policies
        Configure config = Configure.builder()
                .bind("indexes", new IndexTablePolicy())
                .bind("columns", new ColumnTablePolicy())
                .build();
        
        XWPFTemplate template = XWPFTemplate.compile(new ClassPathResource(templatePath).getInputStream(), config).render(data);
        
        // Save the document
        try (OutputStream out = new FileOutputStream(outputPath)) {
            template.write(out);
        }
        template.close();
    }
    
    /**
     * Prepare data for the Word template
     * 
     * @param metadata Database metadata
     * @return Map of template variables and their values
     */
    private Map<String, Object> prepareTemplateData(DatabaseMetadata metadata) {
        Map<String, Object> data = new HashMap<>();
        
        // Database information
        data.put("databaseName", metadata.getDatabaseName());
        data.put("databaseType", metadata.getDatabaseType());
        data.put("databaseVersion", metadata.getDatabaseVersion());
        
        // Tables information
        List<Map<String, Object>> tables = new ArrayList<>();
        for (TableMetadata table : metadata.getTables()) {
            Map<String, Object> tableData = new HashMap<>();
            
            tableData.put("tableName", table.getTableName());
            tableData.put("tableComment", table.getTableComment() != null ? table.getTableComment() : "");
            tableData.put("tableSpace", table.getTableSpace() != null ? table.getTableSpace() : "");
            tableData.put("schema", table.getSchema() != null ? table.getSchema() : "");
            
            // Primary keys
            tableData.put("primaryKeys", String.join(", ", table.getPrimaryKeys()));
            
            // Logical keys
            tableData.put("logicalKeys", String.join(", ", table.getLogicalKeys()));
            
            // Prepare index data
            IndexTableData indexData = prepareIndexData(table.getIndexes());
            tableData.put("indexes", indexData);
            
            // Prepare column data
            ColumnTableData columnData = prepareColumnData(table.getColumns());
            tableData.put("columns", columnData);
            
            tables.add(tableData);
        }
        
        data.put("tables", tables);
        
        return data;
    }
    
    /**
     * Prepare index table data
     */
    private IndexTableData prepareIndexData(List<IndexMetadata> indexes) {
        IndexTableData indexData = new IndexTableData();
        List<Map<String, String>> rows = new ArrayList<>();
        
        for (IndexMetadata index : indexes) {
            Map<String, String> row = new HashMap<>();
            row.put("name", index.getIndexName() != null ? index.getIndexName() : "");
            row.put("type", index.isUnique() ? "Unique" : "Non-unique");
            row.put("columns", String.join(", ", index.getColumnNames()));
            row.put("indexType", index.getIndexType() != null ? index.getIndexType() : "");
            rows.add(row);
        }
        
        indexData.setIndexes(rows);
        return indexData;
    }
    
    /**
     * Prepare column table data
     */
    private ColumnTableData prepareColumnData(List<ColumnMetadata> columns) {
        ColumnTableData columnData = new ColumnTableData();
        List<Map<String, String>> rows = new ArrayList<>();
        
        for (ColumnMetadata column : columns) {
            String dataTypeInfo = column.getDataType();
            if (column.getColumnSize() != null && column.getColumnSize() > 0) {
                dataTypeInfo += "(" + column.getColumnSize();
                if (column.getDecimalDigits() != null && column.getDecimalDigits() > 0) {
                    dataTypeInfo += "," + column.getDecimalDigits();
                }
                dataTypeInfo += ")";
            }
            
            Map<String, String> row = new HashMap<>();
            row.put("position", String.valueOf(column.getOrdinalPosition()));
            row.put("name", column.getColumnName());
            row.put("comment", column.getColumnComment() != null ? column.getColumnComment() : "");
            row.put("type", dataTypeInfo);
            row.put("pk", column.isPrimaryKey() ? "Yes" : "No");
            row.put("nullable", column.isNullable() ? "Yes" : "No");
            row.put("default", column.getDefaultValue() != null ? column.getDefaultValue() : "");
            row.put("fk", column.isForeignKey() ? 
                    column.getForeignKeyTable() + "." + column.getForeignKeyColumn() : "");
            rows.add(row);
        }
        
        columnData.setColumns(rows);
        return columnData;
    }
    
    /**
     * Inner class for index table data
     */
    public static class IndexTableData {
        private List<Map<String, String>> indexes;
        
        public List<Map<String, String>> getIndexes() {
            return indexes;
        }
        
        public void setIndexes(List<Map<String, String>> indexes) {
            this.indexes = indexes;
        }
    }
    
    /**
     * Inner class for column table data
     */
    public static class ColumnTableData {
        private List<Map<String, String>> columns;
        
        public List<Map<String, String>> getColumns() {
            return columns;
        }
        
        public void setColumns(List<Map<String, String>> columns) {
            this.columns = columns;
        }
    }
    
    /**
     * Dynamic render policy for index table
     */
    public static class IndexTablePolicy extends DynamicTableRenderPolicy {
        @Override
        public void render(XWPFTable table, Object data) throws Exception {
            if (null == data) return;
            
            IndexTableData indexData = (IndexTableData) data;
            List<Map<String, String>> indexes = indexData.getIndexes();
            
            if (indexes == null || indexes.isEmpty()) {
                // 如果没有索引数据，则删除表格的第二行（保留表头）
                if (table.getNumberOfRows() > 1) {
                    table.removeRow(1);
                }
                return;
            }
            
            // 表头在第0行，数据从第1行开始
            if (table.getNumberOfRows() > 1) {
                table.removeRow(1); // 删除模板中的占位行
            }
            
            // 添加索引数据行
            for (int i = 0; i < indexes.size(); i++) {
                XWPFTableRow row = table.createRow();
                Map<String, String> rowData = indexes.get(i);
                
                // 确保行有足够的单元格
                while (row.getTableCells().size() < 4) { // 索引表有4列
                    row.createCell();
                }
                
                // 填充单元格
                row.getCell(0).setText(rowData.get("name"));
                row.getCell(1).setText(rowData.get("type"));
                row.getCell(2).setText(rowData.get("columns"));
                row.getCell(3).setText(rowData.get("indexType"));
            }
        }
    }
    
    /**
     * Dynamic render policy for column table
     */
    public static class ColumnTablePolicy extends DynamicTableRenderPolicy {
        @Override
        public void render(XWPFTable table, Object data) throws Exception {
            if (null == data) return;
            
            ColumnTableData columnData = (ColumnTableData) data;
            List<Map<String, String>> columns = columnData.getColumns();
            
            if (columns == null || columns.isEmpty()) {
                // 如果没有列数据，则删除表格的第二行（保留表头）
                if (table.getNumberOfRows() > 1) {
                    table.removeRow(1);
                }
                return;
            }
            
            // 表头在第0行，数据从第1行开始
            if (table.getNumberOfRows() > 1) {
                table.removeRow(1); // 删除模板中的占位行
            }
            
            // 添加列数据行
            for (int i = 0; i < columns.size(); i++) {
                XWPFTableRow row = table.createRow();
                Map<String, String> rowData = columns.get(i);
                
                // 确保行有足够的单元格
                while (row.getTableCells().size() < 8) { // 列表有8列
                    row.createCell();
                }
                
                // 填充单元格
                row.getCell(0).setText(rowData.get("position"));
                row.getCell(1).setText(rowData.get("name"));
                row.getCell(2).setText(rowData.get("comment"));
                row.getCell(3).setText(rowData.get("type"));
                row.getCell(4).setText(rowData.get("pk"));
                row.getCell(5).setText(rowData.get("nullable"));
                row.getCell(6).setText(rowData.get("default"));
                row.getCell(7).setText(rowData.get("fk"));
            }
        }
    }
} 