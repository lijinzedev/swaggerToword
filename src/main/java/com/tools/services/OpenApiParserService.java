package com.tools.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * OpenAPI解析服务
 * 根据配置选择具体的解析策略
 */
@Service
public class OpenApiParserService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonOpenApiParser jsonOpenApiParser;
    private final ObjectOpenApiParser objectOpenApiParser;
    
    @Value("${openapi.parser.use-object-model:false}")
    private boolean useObjectModel;

    @Autowired
    public OpenApiParserService(JsonOpenApiParser jsonOpenApiParser, ObjectOpenApiParser objectOpenApiParser) {
        this.jsonOpenApiParser = jsonOpenApiParser;
        this.objectOpenApiParser = objectOpenApiParser;
    }

    /**
     * 构建数据模型（根据配置选择解析方式）
     * 
     * @param openApiContent OpenAPI文档内容
     * @return 包含数据模型的Map
     * @throws JsonProcessingException 如果处理JSON出错
     */
    public Map<String, Object> buildDataModel(String openApiContent) throws JsonProcessingException {
        return useObjectModel 
            ? objectOpenApiParser.buildDataModel(openApiContent) 
            : jsonOpenApiParser.buildDataModel(openApiContent);
    }

    /**
     * 构建数据模型（通过JSON节点构建）
     * 
     * @param rootNode OpenAPI文档的JSON根节点
     * @return 包含数据模型的Map
     * @throws JsonProcessingException 如果处理JSON出错
     */
    public Map<String, Object> buildDataModel(JsonNode rootNode) throws JsonProcessingException {
        if (useObjectModel) {
            // 将JsonNode转为字符串，然后通过对象模型解析器解析
            String jsonContent = mapper.writeValueAsString(rootNode);
            return objectOpenApiParser.buildDataModel(jsonContent);
        } else {
            return jsonOpenApiParser.buildDataModel(rootNode);
        }
    }
    
    /**
     * 设置解析模式
     * 
     * @param useObjectModel 是否使用对象模型解析（true: 使用对象模型, false: 使用JSON解析）
     */
    public void setParsingMode(boolean useObjectModel) {
        this.useObjectModel = useObjectModel;
    }
}