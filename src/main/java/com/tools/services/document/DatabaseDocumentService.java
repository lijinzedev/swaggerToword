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
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据库文档生成服务
 * 负责将数据库元数据转换为Word文档格式的数据库设计文档
 */
@Service
public class DatabaseDocumentService {

    private static final int TABLE_DETAIL_START_ROW = 8;

    /**
     * 生成数据库文档
     *
     * @param metadata     数据库元数据
     * @param templatePath Word模板路径（相对于classpath）
     * @param outputPath   输出文档路径
     * @throws IOException 文档生成过程中发生IO异常
     */
    public void generateDocument(DatabaseMetadata metadata, String templatePath, String outputPath) throws IOException {
        // 确保输出目录存在
        ensureOutputDirectoryExists(outputPath);
        
        // 准备模板数据模型
        Map<String, Object> dataModel = prepareTemplateData(metadata);

        // 配置并渲染模板
        Configure config = Configure.builder()
                .bind("detail_table", new ColumnDetailTablePolicy())
                .useSpringEL()
                .build();

        try (XWPFTemplate template = XWPFTemplate.compile(
                new ClassPathResource(templatePath).getInputStream(),
                config
        ).render(dataModel);
             OutputStream out = new FileOutputStream(outputPath)) {
            // 保存文档
            template.write(out);
        }
    }

    /**
     * 确保输出目录存在
     */
    private void ensureOutputDirectoryExists(String outputPath) throws IOException {
        Path path = Paths.get(outputPath);
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * 将数据库元数据转换为模板数据模型
     */
    private Map<String, Object> prepareTemplateData(DatabaseMetadata metadata) {
        Map<String, Object> dataModel = new HashMap<>();
        List<Map<String, Object>> resources = new ArrayList<>();

        int tableNo = 1;

        // 处理数据库中的每个表
        for (TableMetadata table : metadata.getTables()) {
            Map<String, Object> tableData = new HashMap<>();

            // 设置表数据
            tableData.put("no", String.valueOf(tableNo++));
            tableData.put("table_name", table.getTableName());
            tableData.put("table_comment", table.getTableComment() != null ? table.getTableComment() : "");
            tableData.put("primaryKeys", String.join(", ", table.getPrimaryKeys()));
            tableData.put("logicalKeys", formatListOrDefault(table.getLogicalKeys(), "无"));
            tableData.put("schema", table.getSchema() != null ? table.getSchema() : "");
            tableData.put("index", formatIndexes(table.getIndexes()));

            // 添加列数据（由策略类渲染）
            tableData.put("detail_table", table.getColumns());

            resources.add(tableData);
        }

        dataModel.put("resources", resources);
        return dataModel;
    }

    /**
     * 格式化列表数据，如果为空则返回默认值
     */
    private String formatListOrDefault(List<String> list, String defaultValue) {
        return list.isEmpty() ? defaultValue : String.join(", ", list);
    }

    /**
     * 格式化索引信息
     */
    private String formatIndexes(List<IndexMetadata> indexes) {
        return indexes.isEmpty() 
            ? "无" 
            : indexes.stream()
                .map(IndexMetadata::getIndexName)
                .collect(Collectors.joining(", "));
    }

    /**
     * 列详情表格渲染策略
     * 负责动态生成表格中的列信息部分
     */
    public static class ColumnDetailTablePolicy extends DynamicTableRenderPolicy {
        
        @Override
        public void render(XWPFTable table, Object data) throws Exception {
            if (data == null) return;

            @SuppressWarnings("unchecked")
            List<ColumnMetadata> columns = (List<ColumnMetadata>) data;

            if (columns == null || columns.isEmpty()) {
                // 如果没有列数据，则删除表格的模板行
                table.removeRow(TABLE_DETAIL_START_ROW);
                return;
            }

            // 按照序号（ordinalPosition）升序排序
            List<ColumnMetadata> sortedColumns = columns.stream()
                    .sorted(Comparator.comparing(ColumnMetadata::getOrdinalPosition))
                    .collect(Collectors.toList());

            // 准备行数据
            List<RowRenderData> rowRenderData = prepareRowData(sortedColumns);

            // 删除模板行
            table.removeRow(TABLE_DETAIL_START_ROW);

            // 添加实际数据行
            renderTableRows(table, rowRenderData);
        }

        /**
         * 准备表格行数据
         */
        private List<RowRenderData> prepareRowData(List<ColumnMetadata> columns) {
            return columns.stream().map(column -> {
                return Rows.of(
                        String.valueOf(column.getOrdinalPosition()),      // 序号
                        column.getColumnComment() != null ? column.getColumnComment() : "",  // 中文名称
                        column.getColumnName(),                           // 列名
                        column.getFormattedDataType(),                    // 数据类型
                        column.getColumnSize() != null ? column.getColumnSize().toString() : "",  // 长度
                        column.isPrimaryKey() ? "是" : "",                // 主键
                        column.isNullable() ? "" : "是",                  // 非空
                        column.getForeignKeyReference()                   // 外键
                ).center().create();
            }).collect(Collectors.toList());
        }

        /**
         * 渲染表格行
         */
        private void renderTableRows(XWPFTable table, List<RowRenderData> rowData) throws Exception {
            for (int i = 0; i < rowData.size(); i++) {
                XWPFTableRow insertNewTableRow = table.insertNewTableRow(TABLE_DETAIL_START_ROW + i);
                for (int j = 0; j < 9; j++) insertNewTableRow.createCell();
                TableTools.mergeCellsHorizonal(table, TABLE_DETAIL_START_ROW + i, 1, 2);
                TableRenderPolicy.Helper.renderRow(table.getRow(TABLE_DETAIL_START_ROW + i), rowData.get(i));
            }
        }
    }
}