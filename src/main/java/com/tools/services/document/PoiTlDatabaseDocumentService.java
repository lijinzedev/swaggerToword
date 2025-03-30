package com.tools.services.document;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.Texts;
import com.deepoove.poi.policy.DynamicTableRenderPolicy;
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
 * Service for generating Word documents from database metadata using poi-tl
 * This implementation is aligned with the model entities
 */
public class PoiTlDatabaseDocumentService {
    
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
                .bind("detail_table", new DetailTablePolicy())
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
        List<Map<String, Object>> resources = new ArrayList<>();
        int tableNo = 1;
        
        for (TableMetadata table : metadata.getTables()) {
            Map<String, Object> tableData = new HashMap<>();
            
            // Add all table properties based on your template
            tableData.put("no", String.valueOf(tableNo++));
            tableData.put("table_name", table.getTableName());
            tableData.put("table_comment", table.getTableComment() != null ? table.getTableComment() : "");
            tableData.put("table_space", table.getTableSpace() != null ? table.getTableSpace() : "mining");
            
            // Primary keys
            tableData.put("primary_keys", String.join(", ", table.getPrimaryKeys()));
            
            // Logical keys
            String logicalKeys = table.getLogicalKeys().isEmpty() ? "无" : String.join(", ", table.getLogicalKeys());
            tableData.put("logical_keys", logicalKeys);
            
            // Add index and column data to be rendered by the policy
            tableData.put("indexes", table.getIndexes());
            tableData.put("columns", table.getColumns());
            tableData.put("detail_table", table.getColumns()); // This will trigger the table policy
            
            resources.add(tableData);
        }
        
        data.put("resources", resources);
        
        return data;
    }
    
    /**
     * Dynamic render policy for column detail table
     */
    public static class DetailTablePolicy extends DynamicTableRenderPolicy {
        @Override
        public void render(XWPFTable table, Object data) throws Exception {
            if (null == data) return;
            
            @SuppressWarnings("unchecked")
            List<ColumnMetadata> columns = (List<ColumnMetadata>) data;
            
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
                ColumnMetadata column = columns.get(i);
                XWPFTableRow row = table.createRow();
                
                // 确保行有足够的单元格
                while (row.getTableCells().size() < 8) { // 列表有8列
                    row.createCell();
                }
                
                // 构建数据类型字符串
                String dataTypeInfo = column.getDataType();
                if (column.getColumnSize() != null && column.getColumnSize() > 0) {
                    dataTypeInfo += "(" + column.getColumnSize();
                    if (column.getDecimalDigits() != null && column.getDecimalDigits() > 0) {
                        dataTypeInfo += "," + column.getDecimalDigits();
                    }
                    dataTypeInfo += ")";
                }
                
                // 填充单元格
                row.getCell(0).setText(String.valueOf(column.getOrdinalPosition()));
                row.getCell(1).setText(column.getColumnComment() != null ? column.getColumnComment() : "");
                row.getCell(2).setText(column.getColumnName());
                row.getCell(3).setText(dataTypeInfo);
                row.getCell(4).setText(String.valueOf(column.getColumnSize() != null ? column.getColumnSize() : ""));
                row.getCell(5).setText(column.isPrimaryKey() ? "是" : "");
                row.getCell(6).setText(column.isNullable() ? "" : "是");
                row.getCell(7).setText(column.isForeignKey() ? 
                        column.getForeignKeyTable() + "." + column.getForeignKeyColumn() : "");
            }
        }
    }
    
    /**
     * Simple demo for testing with the sample_postgresql_metadata.json file
     */
    public static void main(String[] args) {
        try {
            // This assumes you have a method to load the JSON file into DatabaseMetadata object
            DatabaseMetadata metadata = loadSampleMetadata();
            String templatePath = "templates/database_template.docx";
            String outputPath = "output/database_documentation.docx";
            
            PoiTlDatabaseDocumentService service = new PoiTlDatabaseDocumentService();
            service.generateDocument(metadata, templatePath, outputPath);
            
            System.out.println("Document generated successfully at: " + outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sample method to load metadata from a file for testing
     * In a real application, this would come from your DatabaseMetadataService or similar
     */
    private static DatabaseMetadata loadSampleMetadata() {
        // In a real implementation, you would parse the JSON file here
        // For demo purposes, we'll just create a dummy metadata object
        DatabaseMetadata metadata = new DatabaseMetadata();
        metadata.setDatabaseName("ecommerce_db");
        metadata.setDatabaseType("PostgreSQL");
        metadata.setDatabaseVersion("14.5");
        
        // Create a sample table
        TableMetadata table = new TableMetadata();
        table.setTableName("users");
        table.setTableComment("用户账户信息表");
        table.setSchema("public");
        table.setTableSpace("pg_default");
        table.addPrimaryKey("user_id");
        table.addLogicalKey("email");
        
        // Add some columns
        ColumnMetadata col1 = new ColumnMetadata();
        col1.setColumnName("user_id");
        col1.setColumnComment("用户唯一标识ID");
        col1.setDataType("SERIAL");
        col1.setColumnSize(10);
        col1.setPrimaryKey(true);
        col1.setNullable(false);
        col1.setOrdinalPosition(1);
        table.addColumn(col1);
        
        ColumnMetadata col2 = new ColumnMetadata();
        col2.setColumnName("email");
        col2.setColumnComment("用户电子邮箱地址");
        col2.setDataType("VARCHAR");
        col2.setColumnSize(255);
        col2.setPrimaryKey(false);
        col2.setNullable(false);
        col2.setOrdinalPosition(2);
        table.addColumn(col2);
        
        // Add an index
        IndexMetadata idx = new IndexMetadata();
        idx.setIndexName("users_email_idx");
        idx.setUnique(true);
        idx.addColumnName("email");
        idx.setIndexType("BTREE");
        table.addIndex(idx);
        
        metadata.addTable(table);
        
        return metadata;
    }
} 