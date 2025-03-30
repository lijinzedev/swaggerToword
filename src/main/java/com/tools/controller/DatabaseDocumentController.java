package com.tools.controller;

import com.tools.model.database.DatabaseConnectionConfig;
import com.tools.model.database.DatabaseMetadata;
import com.tools.services.database.CustomMetadataParser;
import com.tools.services.database.DatabaseMetadataExtractor;
import com.tools.services.database.DatabaseMetadataExtractorFactory;
import com.tools.services.document.DatabaseDocumentService;
import com.tools.services.document.DatabaseMetadataParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Controller for database metadata extraction and document generation
 */
@RestController
@RequestMapping("/api/database")
public class DatabaseDocumentController {
    
    @Autowired
    private CustomMetadataParser customMetadataParser;
    
    @Autowired
    private DatabaseDocumentService databaseDocumentService;
    

    // Create a new instance of our JSON metadata parser
    private final DatabaseMetadataParser databaseMetadataParser = new DatabaseMetadataParser();
    
    @Value("${templates.path:sql/}")
    private String templatesPath;

    @Value("${output.path:./output}")
    private String outputPath;
    
    /**
     * Generate document from database connection
     * 
     * @param config Database connection configuration
     * @return Response with document file
     */
    @PostMapping("/document/generate")
    public ResponseEntity<byte[]> generateDocument(@RequestBody DatabaseConnectionConfig config) throws IOException {
        // Create output directory if it doesn't exist
        createOutputDirectory();
        
        // Generate unique filename for output
        String outputFileName = "db_" + UUID.randomUUID().toString() + ".docx";
        String outputFilePath = outputPath + File.separator + outputFileName;
        
        // Extract metadata from database
        DatabaseMetadataExtractor extractor = DatabaseMetadataExtractorFactory.getExtractor(config.getDatabaseType());
        DatabaseMetadata metadata = extractor.extractMetadata(config);
        
        // Generate document using our new service
        databaseDocumentService.generateDocument(metadata,
                templatesPath + File.separator + "database_template.docx",
                outputFilePath);
        
        // Return document as response
        Path path = Paths.get(outputFilePath);
        byte[] document = Files.readAllBytes(path);
        
        return createDocumentResponse(document, outputFileName);
    }
    
    /**
     * Generate document from uploaded metadata file
     * 
     * @param file Uploaded metadata file (JSON format)
     * @return Response with document file
     */
    @PostMapping("/document/upload")
    public ResponseEntity<byte[]> generateDocumentFromUpload(@RequestParam("file") MultipartFile file) 
            throws IOException {
        // Create output directory if it doesn't exist
        createOutputDirectory();
        
        // Generate unique filename for output
        String outputFileName = "db_" + UUID.randomUUID().toString() + ".docx";
        String outputFilePath = outputPath + File.separator + outputFileName;
        
        // 直接读取上传文件的内容
        String jsonContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        
        // 解析JSON内容为数据库元数据对象
        DatabaseMetadata metadata = databaseMetadataParser.parseFromString(jsonContent);
        
        // 生成文档
        databaseDocumentService.generateDocument(metadata,
                templatesPath + File.separator + "database_template.docx",
                outputFilePath);
        
        // 返回生成的文档
        Path path = Paths.get(outputFilePath);
        byte[] document = Files.readAllBytes(path);
        
        return createDocumentResponse(document, outputFileName);
    }
    
    /**
     * Extract metadata from database connection
     * 
     * @param config Database connection configuration
     * @return Database metadata
     */
    @PostMapping("/metadata/extract")
    public ResponseEntity<DatabaseMetadata> extractMetadata(@RequestBody DatabaseConnectionConfig config) {
        DatabaseMetadataExtractor extractor = DatabaseMetadataExtractorFactory.getExtractor(config.getDatabaseType());
        DatabaseMetadata metadata = extractor.extractMetadata(config);
        
        return ResponseEntity.ok(metadata);
    }
    
    /**
     * Extract metadata for a specific table
     * 
     * @param config Database connection configuration
     * @param tableName Table name
     * @param schema Schema name (optional)
     * @return Database metadata for the specified table
     */
    @PostMapping("/metadata/table")
    public ResponseEntity<DatabaseMetadata> extractTableMetadata(
            @RequestBody DatabaseConnectionConfig config,
            @RequestParam("tableName") String tableName,
            @RequestParam(value = "schema", required = false) String schema) {
        
        DatabaseMetadataExtractor extractor = DatabaseMetadataExtractorFactory.getExtractor(config.getDatabaseType());
        DatabaseMetadata metadata = extractor.extractTableMetadata(config, tableName, schema);
        
        return ResponseEntity.ok(metadata);
    }
    
    /**
     * Create output directory if it doesn't exist
     */
    private void createOutputDirectory() throws IOException {
        Path outputDirPath = Paths.get(outputPath);
        if (!Files.exists(outputDirPath)) {
            Files.createDirectories(outputDirPath);
        }
    }
    
    /**
     * Create HTTP response with document
     */
    private ResponseEntity<byte[]> createDocumentResponse(byte[] document, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(document);
    }
} 