package com.tools.services.document;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Rows;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating Word documents from database metadata
 */
public class DatabaseDocumentService {

    /**
     * Generate a Word document from database metadata
     *
     * @param metadata     Database metadata
     * @param templatePath Path to the Word template
     * @param outputPath   Path for the generated document
     * @throws IOException If an error occurs during document generation
     */
    public void generateDocument(DatabaseMetadata metadata, String templatePath, String outputPath) throws IOException {
        // Prepare data model for template
        Map<String, Object> dataModel = prepareTemplateData(metadata);

        // Configure and render the template with custom policies
        Configure config = Configure.builder()
                .bind("detail_table", new DetailTablePolicy())
                .useSpringEL()
                .build();

        XWPFTemplate template = XWPFTemplate.compile(
                new ClassPathResource(templatePath).getInputStream(),
                config
        ).render(dataModel);

        // Save the document
        try (OutputStream out = new FileOutputStream(outputPath)) {
            template.write(out);
        }
        template.close();
    }

    /**
     * Convert database metadata to template data model
     */
    private Map<String, Object> prepareTemplateData(DatabaseMetadata metadata) {
        Map<String, Object> dataModel = new HashMap<>();
        List<Map<String, Object>> resources = new ArrayList<>();

        int tableNo = 1;

        // Process each table in the database
        for (TableMetadata table : metadata.getTables()) {
            Map<String, Object> tableData = new HashMap<>();

            // Set table data
            tableData.put("no", String.valueOf(tableNo++));
            tableData.put("table_name", table.getTableName());
            tableData.put("table_comment", table.getTableComment() != null ? table.getTableComment() : "");
            tableData.put("primaryKeys", String.join(", ", table.getPrimaryKeys()));
            tableData.put("logicalKeys", table.getLogicalKeys().isEmpty() ? "无" : String.join(", ", table.getLogicalKeys()));
            tableData.put("schema", table.getSchema() != null ? table.getSchema() : "");
            tableData.put("index", table.getIndexes().isEmpty() ? "无" : table.getIndexes().stream().map(IndexMetadata::getIndexName).collect(Collectors.joining(", ")));

            // Add columns to be rendered by the policy
            tableData.put("detail_table", table.getColumns());

            resources.add(tableData);
        }

        dataModel.put("resources", resources);
        return dataModel;
    }

    /**
     * Table policy for rendering column details
     */
    public static class DetailTablePolicy extends DynamicTableRenderPolicy {
        int startRow = 8;

        @Override
        public void render(XWPFTable table, Object data) throws Exception {
            if (null == data) return;

            @SuppressWarnings("unchecked")
            List<ColumnMetadata> columns = (List<ColumnMetadata>) data;

            if (columns == null || columns.isEmpty()) {
                // 如果没有列数据，则删除表格的第二行（保留表头）
                table.removeRow(startRow);
                return;
            }

            // 按照序号（ordinalPosition）升序排序
            List<ColumnMetadata> sortedColumns = columns.stream()
                    .sorted(Comparator.comparing(ColumnMetadata::getOrdinalPosition).reversed())
                    .collect(Collectors.toList());

            List<RowRenderData> rowRenderData = sortedColumns.stream().map(column -> {
                // 构建数据类型字符串
                String dataTypeInfo = column.getDataType();
                if (column.getColumnSize() != null && column.getColumnSize() > 0) {
                    dataTypeInfo += "(" + column.getColumnSize();
                    if (column.getDecimalDigits() != null && column.getDecimalDigits() > 0) {
                        dataTypeInfo += "," + column.getDecimalDigits();
                    }
                    dataTypeInfo += ")";
                }

                // 长度
                String length = column.getColumnSize() != null ? column.getColumnSize().toString() : "";

                // 序号
                String columnNumber = String.valueOf(column.getOrdinalPosition());

                // 中文名称
                String columnComment = column.getColumnComment() != null ? column.getColumnComment() : "";

                // 列名
                String columnName = column.getColumnName();

                // 主键
                String primaryKey = column.isPrimaryKey() ? "是" : "";

                // 非空
                String isNullable = column.isNullable() ? "" : "是";

                // 外键
                String foreignKey = column.isForeignKey() ?
                        column.getForeignKeyTable() + "." + column.getForeignKeyColumn() : "";

                return Rows.of(
                        columnNumber,    // 序号
                        columnComment,   // 中文名称
                        columnName,      // 列名
                        dataTypeInfo,    // 数据类型
                        length,          // 长度
                        primaryKey,      // 主键
                        isNullable,      // 非空
                        foreignKey       // 外键
                ).center().create();
            }).collect(Collectors.toList());

            // 删除模板行
            table.removeRow(startRow);

            for (int i = 0; i < rowRenderData.size(); i++) {
                XWPFTableRow insertNewTableRow = table.insertNewTableRow(startRow);
                for (int j = 0; j < 9; j++) insertNewTableRow.createCell();
                TableTools.mergeCellsHorizonal(table, startRow, 1, 2);
                TableRenderPolicy.Helper.renderRow(table.getRow(startRow), rowRenderData.get(i));
            }
        }
    }
}