package com.tools.services;

import com.deepoove.poi.data.BookmarkTextRenderData;
import com.deepoove.poi.data.HyperlinkTextRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.highight.HighlightRenderData;
import com.tools.highight.HighlightStyle;
import com.tools.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JsonOpenApiParser implements OpenApiParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> buildDataModel(String openApiContent) throws JsonProcessingException {
        // 为了保持接口兼容性，转换结构化模型为Map
        ApiDataModel structuredModel = buildStructuredDataModel(openApiContent);
        return structuredModel.toMap();
    }

    @Override
    public ApiDataModel buildStructuredDataModel(String openApiContent) throws JsonProcessingException {
        JsonNode rootNode = mapper.readTree(openApiContent);
        return buildStructuredDataModel(rootNode);
    }

    /**
     * 构建结构化数据模型（通过JSON节点构建）
     */
    public ApiDataModel buildStructuredDataModel(JsonNode rootNode) throws JsonProcessingException {
        ApiDataModel dataModel = new ApiDataModel();

        // 解析info部分
        dataModel.setInfo(parseApiInfo(rootNode));

        // 解析paths部分 (接口列表)
        List<ApiResource> resources = parseApiResources(rootNode);
        for (ApiResource resource : resources) {
            dataModel.addResource(resource);
        }

        // 解析components/schemas部分 (数据模型)
        List<Definition> definitions = parseDefinitions(rootNode);
        for (Definition definition : definitions) {
            dataModel.addDefinition(definition);
        }

        return dataModel;
    }

    /**
     * 解析API信息
     */
    private ApiInfo parseApiInfo(JsonNode rootNode) {
        JsonNode infoNode = rootNode.get("info");
        if (infoNode == null) {
            return new ApiInfo();
        }

        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setTitle(getTextValue(infoNode, "title"));
        apiInfo.setDescription(getTextValue(infoNode, "description"));
        apiInfo.setVersion(getTextValue(infoNode, "version"));

        // 解析联系人信息
        if (infoNode.has("contact")) {
            JsonNode contactNode = infoNode.get("contact");
            ApiInfo.ContactInfo contact = new ApiInfo.ContactInfo();
            contact.setEmail(getTextValue(contactNode, "email"));
            apiInfo.setContact(contact);
        }

        // 解析许可证信息
        if (infoNode.has("license")) {
            JsonNode licenseNode = infoNode.get("license");
            ApiInfo.LicenseInfo license = new ApiInfo.LicenseInfo();
            license.setName(getTextValue(licenseNode, "name"));
            apiInfo.setLicense(license);
        }

        return apiInfo;
    }

    /**
     * 解析API资源路径
     */
    private List<ApiResource> parseApiResources(JsonNode rootNode) {
        Map<String, ApiResource> resourceMap = new HashMap<>();
        JsonNode pathsNode = rootNode.get("paths");

        if (pathsNode != null) {
            Iterator<Map.Entry<String, JsonNode>> pathsFields = pathsNode.fields();
            while (pathsFields.hasNext()) {
                Map.Entry<String, JsonNode> pathEntry = pathsFields.next();
                String path = pathEntry.getKey();
                JsonNode pathItemNode = pathEntry.getValue();

                // 处理每个HTTP方法
                parseOperations(resourceMap, path, pathItemNode);
            }
        }

        return new ArrayList<>(resourceMap.values());
    }

    /**
     * 解析API操作
     */
    private void parseOperations(Map<String, ApiResource> resourceMap, String path, JsonNode pathItemNode) {
        Iterator<Map.Entry<String, JsonNode>> operations = pathItemNode.fields();
        while (operations.hasNext()) {
            Map.Entry<String, JsonNode> operationEntry = operations.next();
            String httpMethod = operationEntry.getKey();
            JsonNode operationNode = operationEntry.getValue();

            // 找到对应的tag作为resource分组
            String tag = "Default";
            if (operationNode.has("tags") && operationNode.get("tags").size() > 0) {
                tag = operationNode.get("tags").get(0).asText();
            }

            // 查找或创建resource
            ApiResource resource = resourceMap.get(tag);
            if (resource == null) {
                resource = new ApiResource();
                resource.setName(tag);
                resource.setDescription("");
                resourceMap.put(tag, resource);
            }

            // 创建endpoint
            Endpoint endpoint = createEndpoint(path, httpMethod, operationNode);

            // 添加endpoint到resource
            resource.addEndpoint(endpoint);
        }
    }

    /**
     * 创建端点信息
     */
    private Endpoint createEndpoint(String path, String httpMethod, JsonNode operationNode) {
        Endpoint endpoint = new Endpoint();
        endpoint.setSummary(getTextValue(operationNode, "summary"));
        endpoint.setDescription(getTextValue(operationNode, "description"));
        endpoint.setHttpMethod(httpMethod.toUpperCase());
        endpoint.setUrl(path);

        // 处理produces (Content-Type)
        List<String> produces = parseProduces(operationNode);
        endpoint.setProduces(produces);

        // 处理consumes (请求Content-Type)
        List<String> consumes = parseConsumes(operationNode);
        endpoint.setConsumes(consumes);

        // 处理参数
        List<Parameter> parameters = parseParameters(operationNode);
        endpoint.setParameters(parameters);

        // 处理响应
        List<Response> responses = parseResponses(operationNode);
        endpoint.setResponses(responses);

        return endpoint;
    }

    /**
     * 解析产生的内容类型
     */
    private List<String> parseProduces(JsonNode operationNode) {
        List<String> produces = new ArrayList<>();
        if (operationNode.has("responses")) {
            JsonNode responsesNode = operationNode.get("responses");
            Iterator<Map.Entry<String, JsonNode>> responseFields = responsesNode.fields();
            while (responseFields.hasNext()) {
                Map.Entry<String, JsonNode> responseEntry = responseFields.next();
                JsonNode responseNode = responseEntry.getValue();
                if (responseNode.has("content")) {
                    Iterator<String> contentTypes = responseNode.get("content").fieldNames();
                    while (contentTypes.hasNext()) {
                        String next = contentTypes.next();
                        if (!produces.contains(next)) {
                            produces.add(next);
                        }
                    }
                }
            }
        }
        return produces;
    }

    /**
     * 解析消费的内容类型
     */
    private List<String> parseConsumes(JsonNode operationNode) {
        List<String> consumes = new ArrayList<>();
        if (operationNode.has("requestBody") && operationNode.get("requestBody").has("content")) {
            Iterator<String> contentTypes = operationNode.get("requestBody").get("content").fieldNames();
            while (contentTypes.hasNext()) {
                consumes.add(contentTypes.next());
            }
        }
        return consumes;
    }

    /**
     * 解析参数
     */
    private List<Parameter> parseParameters(JsonNode operationNode) {
        List<Parameter> parameters = new ArrayList<>();

        // 处理常规参数
        if (operationNode.has("parameters")) {
            for (JsonNode paramNode : operationNode.get("parameters")) {
                Parameter parameter = new Parameter();
                parameter.setIn(getTextValue(paramNode, "in"));
                parameter.setName(getTextValue(paramNode, "name"));
                parameter.setDescription(getTextValue(paramNode, "description"));
                parameter.setRequired(paramNode.has("required") && paramNode.get("required").asBoolean());

                // 处理schema
                if (paramNode.has("schema")) {
                    parameter.setSchema(getSchemaType(paramNode.get("schema")));
                }
                parameters.add(parameter);
            }
        }

        // 处理requestBody参数
        if (operationNode.has("requestBody")) {
            JsonNode requestBodyNode = operationNode.get("requestBody");
            boolean required = requestBodyNode.has("required") && requestBodyNode.get("required").asBoolean();
            if (requestBodyNode.has("content")) {
                JsonNode contentNode = requestBodyNode.get("content");
                Iterator<Map.Entry<String, JsonNode>> contentFields = contentNode.fields();
                if (contentFields.hasNext()) {
                    Map.Entry<String, JsonNode> contentEntry = contentFields.next();
                    JsonNode mediaTypeNode = contentEntry.getValue();
                    if (mediaTypeNode.has("schema")) {
                        Parameter parameter = new Parameter();
                        parameter.setIn("body");
                        parameter.setName("body");
                        parameter.setDescription(getTextValue(requestBodyNode, "description"));
                        parameter.setRequired(required);
                        parameter.setSchema(getSchemaType(mediaTypeNode.get("schema")));
                        parameters.add(parameter);
                    }
                }
            }
        }

        return parameters;
    }

    /**
     * 解析响应
     */
    private List<Response> parseResponses(JsonNode operationNode) {
        List<Response> responses = new ArrayList<>();
        if (operationNode.has("responses")) {
            JsonNode responsesNode = operationNode.get("responses");
            Iterator<Map.Entry<String, JsonNode>> responseFields = responsesNode.fields();
            while (responseFields.hasNext()) {
                Map.Entry<String, JsonNode> responseEntry = responseFields.next();
                String code = responseEntry.getKey();
                JsonNode responseNode = responseEntry.getValue();
                
                Response response = new Response();
                response.setCode(code);
                response.setDescription(getTextValue(responseNode, "description"));

                // 处理headers
                if (responseNode.has("headers")) {
                    JsonNode headersNode = responseNode.get("headers");
                    Iterator<Map.Entry<String, JsonNode>> headerFields = headersNode.fields();
                    while (headerFields.hasNext()) {
                        Map.Entry<String, JsonNode> headerEntry = headerFields.next();
                        String headerName = headerEntry.getKey();
                        JsonNode headerNode = headerEntry.getValue();
                        
                        Response.Header header = new Response.Header();
                        header.setName(headerName);
                        header.setDescription(getTextValue(headerNode, "description"));
                        header.setType(getTextValue(headerNode, "schema", "type"));
                        response.addHeader(header);
                    }
                }

                // 处理schema
                if (responseNode.has("content")) {
                    JsonNode contentNode = responseNode.get("content");
                    Iterator<Map.Entry<String, JsonNode>> contentFields = contentNode.fields();
                    if (contentFields.hasNext()) {
                        Map.Entry<String, JsonNode> contentEntry = contentFields.next();
                        JsonNode mediaTypeNode = contentEntry.getValue();
                        if (mediaTypeNode.has("schema")) {
                            response.setSchema(getSchemaType(mediaTypeNode.get("schema")));
                        }
                    }
                } else {
                    response.setSchema(new ArrayList<>());
                }
                
                responses.add(response);
            }
        }
        return responses;
    }

    /**
     * 解析数据模型定义
     */
    private List<Definition> parseDefinitions(JsonNode rootNode) throws JsonProcessingException {
        List<Definition> definitions = new ArrayList<>();
        if (rootNode.has("components") && rootNode.get("components").has("schemas")) {
            JsonNode schemasNode = rootNode.get("components").get("schemas");
            Iterator<Map.Entry<String, JsonNode>> schemaFields = schemasNode.fields();

            while (schemaFields.hasNext()) {
                Map.Entry<String, JsonNode> schemaEntry = schemaFields.next();
                String schemaName = schemaEntry.getKey();
                JsonNode schemaNode = schemaEntry.getValue();

                Definition definition = new Definition();
                definition.setName(new BookmarkTextRenderData(schemaName, schemaName));

                // 处理属性
                if (schemaNode.has("properties")) {
                    JsonNode propertiesNode = schemaNode.get("properties");
                    Iterator<Map.Entry<String, JsonNode>> propertyFields = propertiesNode.fields();

                    // 获取required属性列表
                    List<String> requiredProps = new ArrayList<>();
                    if (schemaNode.has("required")) {
                        for (JsonNode requiredNode : schemaNode.get("required")) {
                            requiredProps.add(requiredNode.asText());
                        }
                    }

                    while (propertyFields.hasNext()) {
                        Map.Entry<String, JsonNode> propertyEntry = propertyFields.next();
                        String propertyName = propertyEntry.getKey();
                        JsonNode propertyNode = propertyEntry.getValue();

                        Definition.Property property = new Definition.Property();
                        property.setName(propertyName);
                        property.setDescription(getTextValue(propertyNode, "description"));
                        property.setRequired(requiredProps.contains(propertyName));
                        property.setSchema(getSchemaType(propertyNode));

                        definition.addProperty(property);
                    }
                }

                // 生成示例代码
                definition.setDefinitionCode(generateExampleJson(schemaNode, schemasNode));
                definitions.add(definition);
            }
        }

        return definitions;
    }

    /**
     * 获取Schema类型
     */
    private List<TextRenderData> getSchemaType(JsonNode schemaNode) {
        List<TextRenderData> schema = new ArrayList<>();
        if (schemaNode == null) {
            return schema;
        }
        if (schemaNode.has("$ref")) {
            String ref = schemaNode.get("$ref").asText();
            String refName = ref.substring(ref.lastIndexOf("/") + 1);
            schema.add(new HyperlinkTextRenderData(refName, "anchor:" + refName));
            return schema;
        }
        String type = getTextValue(schemaNode, "type");
        if ("array".equals(type) && schemaNode.has("items")) {
            schema.add(new TextRenderData("<"));
            schema.addAll(getSchemaType(schemaNode.get("items")));
            schema.add(new TextRenderData(">"));
            schema.add(new TextRenderData("array"));
            return schema;
        }
        if ("object".equals(type) && schemaNode.has("additionalProperties")) {
            schema.add(new TextRenderData("map[string, "));
            schema.addAll(getSchemaType(schemaNode.get("additionalProperties")));
            schema.add(new TextRenderData("]"));
            return schema;
        }
        schema.add(new TextRenderData(type));
        return schema;
    }

    /**
     * 生成示例JSON
     */
    private HighlightRenderData generateExampleJson(JsonNode schemaNode, JsonNode schemasNode) throws JsonProcessingException {
        Map<String, Object> exampleJson = generateExampleObject(schemaNode, schemasNode, new HashSet<>());
        String exampleJsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(exampleJson);

        HighlightRenderData code = new HighlightRenderData();
        code.setCode(exampleJsonStr);
        code.setLanguage("json");
        code.setStyle(HighlightStyle.builder().withTheme("zenburn").build());

        return code;
    }

    /**
     * 生成示例对象
     */
    private Map<String, Object> generateExampleObject(JsonNode objectNode, JsonNode schemasNode, Set<String> processedRefs) {
        Map<String, Object> obj = new LinkedHashMap<>();

        if (objectNode.has("properties")) {
            JsonNode propertiesNode = objectNode.get("properties");
            Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                obj.put(entry.getKey(), generateExampleValue(entry.getValue(), schemasNode, processedRefs));
            }
        } else if (objectNode.has("additionalProperties")) {
            obj.put("key", generateExampleValue(objectNode.get("additionalProperties"), schemasNode, processedRefs));
        }

        return obj;
    }

    /**
     * 生成示例值
     */
    private Object generateExampleValue(JsonNode propertyNode, JsonNode schemasNode, Set<String> processedRefs) {
        // 如果有example字段，优先使用
        if (propertyNode.has("example")) {
            return propertyNode.get("example").asText();
        }

        // 处理引用
        if (propertyNode.has("$ref")) {
            String ref = propertyNode.get("$ref").asText();
            // 解析引用路径，支持OpenAPI 3.0格式的引用
            String[] refParts = ref.split("/");
            String refName = refParts[refParts.length - 1];

            // 避免循环引用
            if (processedRefs.contains(refName)) {
                return ref; // 返回引用路径而不是展开对象
            }

            processedRefs.add(refName);
            if (schemasNode.has(refName)) {
                return generateExampleObject(schemasNode.get(refName), schemasNode, processedRefs);
            }
            return new LinkedHashMap<>(); // 如果找不到引用的定义
        }

        String type = propertyNode.has("type") ? propertyNode.get("type").asText() : "object";

        switch (type) {
            case "string":
                // 如果有format字段，可以生成更合适的示例
                if (propertyNode.has("format")) {
                    String format = propertyNode.get("format").asText();
                    switch (format) {
                        case "date-time":
                            return "2023-01-01T12:00:00Z";
                        case "date":
                            return "2023-01-01";
                        case "email":
                            return "user@example.com";
                        case "uuid":
                            return "550e8400-e29b-41d4-a716-446655440000";
                    }
                }
                return "example";
            case "integer":
            case "number":
                if (propertyNode.has("format")) {
                    String format = propertyNode.get("format").asText();
                    if ("int64".equals(format)) {
                        return 10000000000L;
                    }
                }
                return 0;
            case "boolean":
                return false;
            case "array":
                List<Object> array = new ArrayList<>();
                if (propertyNode.has("items")) {
                    array.add(generateExampleValue(propertyNode.get("items"), schemasNode, processedRefs));
                }
                return array;
            case "object":
                return generateExampleObject(propertyNode, schemasNode, processedRefs);
            default:
                return "unknown";
        }
    }

    /**
     * 辅助方法
     */
    private String getTextValue(JsonNode node, String... fieldPath) {
        JsonNode current = node;
        for (String field : fieldPath) {
            if (current != null && current.has(field)) {
                current = current.get(field);
            } else {
                return "";
            }
        }
        return current != null ? current.asText() : "";
    }
} 