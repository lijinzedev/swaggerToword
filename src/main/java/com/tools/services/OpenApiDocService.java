package com.tools.services;


import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.highight.HighlightRenderPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class OpenApiDocService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OpenApiParserService parserService;

    @Value("${openapi.default-template:swagger/default-swagger-template.docx}")
    private String defaultTemplatePath;

    public OpenApiDocService(OpenApiParserService parserService) {
        this.parserService = parserService;
    }

    /**
     * 从URL生成文档
     */
    public byte[] generateDocFromUrl(String openApiUrl, String templateName) throws IOException {
        String openApiJson = restTemplate.getForObject(openApiUrl, String.class);
        return generateDocFromJson(openApiJson, templateName);
    }

    /**
     * 从URL生成文档（使用自定义模板）
     */
    public byte[] generateDocFromUrl(String openApiUrl, InputStream templateStream) throws IOException {
        String openApiJson = restTemplate.getForObject(openApiUrl, String.class);
        return generateDocFromJson(openApiJson, templateStream);
    }

    /**
     * 从JSON字符串生成文档
     */
    public byte[] generateDocFromJson(String openApiJson, String templateName) throws IOException {
        InputStream templateStream = getTemplateStream(templateName);
        return generateDocFromJson(openApiJson, templateStream);
    }

    /**
     * 从JSON字符串生成文档（使用自定义模板）
     */
    public byte[] generateDocFromJson(String openApiJson, InputStream templateStream) throws IOException {
        JsonNode rootNode = objectMapper.readTree(openApiJson);
        Map<String, Object> dataModel = parserService.buildDataModel(rootNode);

        return renderDocument(dataModel, templateStream);
    }

    /**
     * 从文件生成文档
     */
    public byte[] generateDocFromFile(InputStream jsonFileStream, String templateName) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonFileStream);
        InputStream templateStream = getTemplateStream(templateName);
        Map<String, Object> dataModel = parserService.buildDataModel(rootNode);

        return renderDocument(dataModel, templateStream);
    }

    /**
     * 从文件生成文档（使用自定义模板）
     */
    public byte[] generateDocFromFile(InputStream jsonFileStream, InputStream templateStream) throws IOException {
        JsonNode rootNode = objectMapper.readTree(jsonFileStream);
        Map<String, Object> dataModel = parserService.buildDataModel(rootNode);

        return renderDocument(dataModel, templateStream);
    }

    /**
     * 获取模板输入流
     */
    private InputStream getTemplateStream(String templateName) throws IOException {
        if (templateName != null && !templateName.isEmpty()) {
            return new ClassPathResource("templates/" + templateName).getInputStream();
        } else {
            return new ClassPathResource(defaultTemplatePath).getInputStream();
        }
    }

    /**
     * 渲染文档
     */
    private byte[] renderDocument(Map<String, Object> dataModel, InputStream templateStream) throws IOException {
        Configure config = Configure.builder()
                .bind("parameters", new LoopRowTableRenderPolicy())
                .bind("responses", new LoopRowTableRenderPolicy())
                .bind("properties", new LoopRowTableRenderPolicy())
                .bind("definitionCode", new HighlightRenderPolicy())
                .useSpringEL()
                .build();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        XWPFTemplate template = XWPFTemplate.compile(templateStream, config).render(dataModel);
        template.write(outputStream);
        template.close();

        return outputStream.toByteArray();
    }
}