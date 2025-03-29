package com.tools.controller;


import com.tools.services.OpenApiDocService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/openapi-doc")
@Api(tags = "OpenAPI Documentation Generator", description = "Endpoints for generating documentation from OpenAPI specifications")
public class OpenApiDocController {

    @Autowired
    private OpenApiDocService openApiDocService;

    /**
     * 通过URL生成文档
     */
    @PostMapping("/generate-from-url")
    @ApiOperation(value = "Generate documentation from OpenAPI URL", notes = "Fetches OpenAPI spec from a URL and generates documentation")
    public ResponseEntity<Resource> generateFromUrl(
            @ApiParam(value = "URL to the OpenAPI specification", required = true)
            @RequestParam("url") String openApiUrl,
            @ApiParam(value = "Name of the template to use (optional)")
            @RequestParam(value = "templateName", required = false) String templateName,
            @ApiParam(value = "Custom template file (optional)")
            @RequestParam(value = "template", required = false) MultipartFile templateFile) throws IOException {

        byte[] docBytes;
        if (templateFile != null && !templateFile.isEmpty()) {
            docBytes = openApiDocService.generateDocFromUrl(openApiUrl, templateFile.getInputStream());
        } else {
            docBytes = openApiDocService.generateDocFromUrl(openApiUrl, templateName);
        }

        return createDownloadResponse(docBytes, "openapi-doc.docx");
    }

    /**
     * 通过JSON字符串生成文档
     */
    @PostMapping("/generate-from-json")
    @ApiOperation(value = "Generate documentation from OpenAPI JSON", notes = "Generates documentation from OpenAPI JSON in request body")
    public ResponseEntity<Resource> generateFromJson(
            @ApiParam(value = "OpenAPI specification as JSON string", required = true)
            @RequestBody String openApiJson,
            @ApiParam(value = "Name of the template to use (optional)")
            @RequestParam(value = "templateName", required = false) String templateName,
            @ApiParam(value = "Custom template file (optional)")
            @RequestParam(value = "template", required = false) MultipartFile templateFile) throws IOException {

        byte[] docBytes;
        if (templateFile != null && !templateFile.isEmpty()) {
            docBytes = openApiDocService.generateDocFromJson(openApiJson, templateFile.getInputStream());
        } else {
            docBytes = openApiDocService.generateDocFromJson(openApiJson, templateName);
        }

        return createDownloadResponse(docBytes, "openapi-doc.docx");
    }

    /**
     * 通过上传的JSON文件生成文档
     */
    @PostMapping("/generate-from-file")
    @ApiOperation(value = "Generate documentation from OpenAPI file", notes = "Generates documentation from uploaded OpenAPI JSON file")
    public ResponseEntity<Resource> generateFromFile(
            @ApiParam(value = "OpenAPI specification as JSON file", required = true)
            @RequestParam("file") MultipartFile jsonFile,
            @ApiParam(value = "Name of the template to use (optional)")
            @RequestParam(value = "templateName", required = false) String templateName,
            @ApiParam(value = "Custom template file (optional)")
            @RequestParam(value = "template", required = false) MultipartFile templateFile) throws IOException {

        byte[] docBytes;
        if (templateFile != null && !templateFile.isEmpty()) {
            docBytes = openApiDocService.generateDocFromFile(jsonFile.getInputStream(), templateFile.getInputStream());
        } else {
            docBytes = openApiDocService.generateDocFromFile(jsonFile.getInputStream(), templateName);
        }

        return createDownloadResponse(docBytes, "openapi-doc.docx");
    }

    /**
     * 创建文件下载响应
     */
    private ResponseEntity<Resource> createDownloadResponse(byte[] docBytes, String filename) {
        ByteArrayResource resource = new ByteArrayResource(docBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(docBytes.length)
                .body(resource);
    }
}