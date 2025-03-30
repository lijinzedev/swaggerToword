package com.tools.examples;

import com.tools.model.database.DatabaseMetadata;
import com.tools.services.document.DatabaseMetadataParser;
import com.tools.services.document.PoiTlDatabaseDocumentService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Demonstration class for using the database template with Json metadata
 */
public class DatabaseTemplateDemo {

    public static void main(String[] args) {
        try {
            System.out.println("Starting database template demo...");
            
            // Define file paths
            String jsonFilePath = "sample_postgresql_metadata.json";
            String templatePath = "templates/database_template.docx";
            String outputPath = "output/database_documentation.docx";
            
            // Make sure directories exist
            Files.createDirectories(Paths.get("templates"));
            Files.createDirectories(Paths.get("output"));
            
            // Create a simple template if it doesn't exist yet
            createTemplateIfNeeded(templatePath);
            
            // Parse the JSON metadata
            DatabaseMetadataParser parser = new DatabaseMetadataParser();
            DatabaseMetadata metadata = parser.parseFromFile(new File(jsonFilePath));
            
            System.out.println("Parsed metadata for database: " + metadata.getDatabaseName());
            System.out.println("Found " + metadata.getTables().size() + " tables");
            
            // Generate the document
            PoiTlDatabaseDocumentService service = new PoiTlDatabaseDocumentService();
            service.generateDocument(metadata, templatePath, outputPath);
            
            System.out.println("Document generated successfully at: " + outputPath);
            System.out.println("Done!");
            
        } catch (Exception e) {
            System.err.println("Error in database template demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a simple template file for demonstration purposes
     * In a real application, you would create a proper Word template with the placeholders
     */
    private static void createTemplateIfNeeded(String templatePath) throws Exception {
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            System.out.println("Creating template file for demo...");
            
            // In a real application, you would copy a real template here
            // For demo purposes, we'll just copy a simple one or create it
            
            // Example of creating a template programmatically (simplified)
            String templateContent = "1.1.1基础信息及系统管理模块\n" +
                    "{{?resources}}\n" +
                    "{{no}}、{{table_comment}} {{table_name}}\n" +
                    "中文名称\t{{table_comment}}\n" +
                    "物理表名\t{{table_name}}\n" +
                    "主键\t{{primary_keys}}\n" +
                    "逻辑主键 \t{{logical_keys}}\n" +
                    "所属表空间\t{{table_space}}\n" +
                    "索引\t\n" +
                    "{{detail_table}}字段列表\n" +
                    "序号\t中文名称\t列名\t数据类型\t长度\t主键\t非空\t外键\n" +
                    "\t\t\t\t\t\t\t\n" +
                    "{{/resources}}";
            
            // You would create a Word document with this content
            // For simplicity in this demo, we'll just notify that in a real app you'd use
            // a pre-created Word template
            System.out.println("Template content example (in a real app, create a Word doc with this):");
            System.out.println(templateContent);
            
            // Create an empty file for the purpose of this demo
            // In a real application, you would have a real Word template with the above content
            Files.createFile(templateFile.toPath());
            
            System.out.println("Note: For this demo to work properly, create a Word template at: " + templatePath);
            System.out.println("      with the placeholders shown above formatted appropriately.");
        }
    }
} 