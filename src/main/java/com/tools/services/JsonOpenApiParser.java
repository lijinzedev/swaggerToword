package com.tools.services;

import com.deepoove.poi.data.BookmarkTextRenderData;
import com.deepoove.poi.data.HyperlinkTextRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.highight.HighlightRenderData;
import com.tools.highight.HighlightStyle;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JsonOpenApiParser implements OpenApiParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> buildDataModel(String openApiContent) throws JsonProcessingException {
        JsonNode rootNode = mapper.readTree(openApiContent);
        return buildDataModel(rootNode);
    }

    /**
     * 构建数据模型
     */
    public Map<String, Object> buildDataModel(JsonNode rootNode) throws JsonProcessingException {
        Map<String, Object> dataModel = new HashMap<>();

        // 解析info部分
        parseInfo(rootNode, dataModel);

        // 解析paths部分 (接口列表)
        parseResources(rootNode, dataModel);

        // 解析components/schemas部分 (数据模型)
        parseDefinitions(rootNode, dataModel);

        return dataModel;
    }

    /**
     * 解析API信息
     */
    private void parseInfo(JsonNode rootNode, Map<String, Object> dataModel) {
        JsonNode infoNode = rootNode.get("info");
        Map<String, Object> info = new HashMap<>();
        info.put("title", getTextValue(infoNode, "title"));
        info.put("description", getTextValue(infoNode, "description"));
        info.put("version", getTextValue(infoNode, "version"));

        // 解析联系人信息
        Map<String, String> contact = new HashMap<>();
        if (infoNode.has("contact")) {
            JsonNode contactNode = infoNode.get("contact");
            contact.put("email", getTextValue(contactNode, "email"));
        }
        info.put("contact", contact);

        // 解析许可证信息
        Map<String, String> license = new HashMap<>();
        if (infoNode.has("license")) {
            JsonNode licenseNode = infoNode.get("license");
            license.put("name", getTextValue(licenseNode, "name"));
        }
        info.put("license", license);

        dataModel.put("info", info);
    }

    /**
     * 解析API资源路径
     */
    private void parseResources(JsonNode rootNode, Map<String, Object> dataModel) {
        List<Map<String, Object>> resources = new ArrayList<>();
        JsonNode pathsNode = rootNode.get("paths");

        if (pathsNode != null) {
            Iterator<Map.Entry<String, JsonNode>> pathsFields = pathsNode.fields();
            while (pathsFields.hasNext()) {
                Map.Entry<String, JsonNode> pathEntry = pathsFields.next();
                String path = pathEntry.getKey();
                JsonNode pathItemNode = pathEntry.getValue();

                // 处理每个HTTP方法
                parseOperations(resources, path, pathItemNode);
            }
        }

        dataModel.put("resources", resources);
    }

    /**
     * 解析API操作
     */
    private void parseOperations(List<Map<String, Object>> resources, String path, JsonNode pathItemNode) {
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
            Map<String, Object> resource = findResourceByName(resources, tag);
            if (resource == null) {
                resource = new HashMap<>();
                resource.put("name", tag);
                resource.put("description", "");
                resource.put("endpoints", new ArrayList<Map<String, Object>>());
                resources.add(resource);
            }

            // 创建endpoint
            Map<String, Object> endpoint = createEndpoint(path, httpMethod, operationNode);

            // 添加endpoint到resource
            ((List<Map<String, Object>>) resource.get("endpoints")).add(endpoint);
        }
    }

    /**
     * 创建端点信息
     */
    private Map<String, Object> createEndpoint(String path, String httpMethod, JsonNode operationNode) {
        Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("summary", getTextValue(operationNode, "summary"));
        endpoint.put("description", getTextValue(operationNode, "description"));
        endpoint.put("httpMethod", httpMethod.toUpperCase());
        endpoint.put("url", path);

        // 处理produces (Content-Type)
        parseProduces(operationNode, endpoint);

        // 处理consumes (请求Content-Type)
        parseConsumes(operationNode, endpoint);

        // 处理参数
        parseParameters(operationNode, endpoint);

        // 处理响应
        parseResponses(operationNode, endpoint);

        return endpoint;
    }

    /**
     * 解析产生的内容类型
     */
    private void parseProduces(JsonNode operationNode, Map<String, Object> endpoint) {
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
                        if (!checkHasProduce(produces, next)) {
                            produces.add(next);
                        }
                    }
                }
            }
        }
        endpoint.put("produces", produces);
    }

    /**
     * 解析消费的内容类型
     */
    private void parseConsumes(JsonNode operationNode, Map<String, Object> endpoint) {
        List<String> consumes = new ArrayList<>();
        if (operationNode.has("requestBody") && operationNode.get("requestBody").has("content")) {
            Iterator<String> contentTypes = operationNode.get("requestBody").get("content").fieldNames();
            while (contentTypes.hasNext()) {
                consumes.add(contentTypes.next());
            }
        }
        endpoint.put("consumes", consumes);
    }

    /**
     * 解析参数
     */
    private void parseParameters(JsonNode operationNode, Map<String, Object> endpoint) {
        List<Map<String, Object>> parameters = new ArrayList<>();

        // 处理常规参数
        if (operationNode.has("parameters")) {
            for (JsonNode paramNode : operationNode.get("parameters")) {
                Map<String, Object> parameter = new HashMap<>();
                parameter.put("in", getTextValue(paramNode, "in"));
                parameter.put("name", getTextValue(paramNode, "name"));
                parameter.put("description", getTextValue(paramNode, "description"));
                parameter.put("required", paramNode.has("required") && paramNode.get("required").asBoolean());

                // 处理schema
                if (paramNode.has("schema")) {
                    parameter.put("schema", getSchemaType(paramNode.get("schema")));
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
                        Map<String, Object> parameter = new HashMap<>();
                        parameter.put("in", "body");
                        parameter.put("name", "body");
                        parameter.put("description", getTextValue(requestBodyNode, "description"));
                        parameter.put("required", required);
                        parameter.put("schema", getSchemaType(mediaTypeNode.get("schema")));
                        parameters.add(parameter);
                    }
                }
            }
        }

        endpoint.put("parameters", parameters);
    }

    /**
     * 解析响应
     */
    private void parseResponses(JsonNode operationNode, Map<String, Object> endpoint) {
        List<Map<String, Object>> responses = new ArrayList<>();
        if (operationNode.has("responses")) {
            JsonNode responsesNode = operationNode.get("responses");
            Iterator<Map.Entry<String, JsonNode>> responseFields = responsesNode.fields();
            while (responseFields.hasNext()) {
                Map.Entry<String, JsonNode> responseEntry = responseFields.next();
                String code = responseEntry.getKey();
                JsonNode responseNode = responseEntry.getValue();
                Map<String, Object> response = new HashMap<>();
                response.put("code", code);
                response.put("description", getTextValue(responseNode, "description"));

                // 处理headers
                List<Map<String, Object>> headers = new ArrayList<>();
                if (responseNode.has("headers")) {
                    JsonNode headersNode = responseNode.get("headers");
                    Iterator<Map.Entry<String, JsonNode>> headerFields = headersNode.fields();
                    while (headerFields.hasNext()) {
                        Map.Entry<String, JsonNode> headerEntry = headerFields.next();
                        String headerName = headerEntry.getKey();
                        JsonNode headerNode = headerEntry.getValue();
                        Map<String, Object> header = new HashMap<>();
                        header.put("name", headerName);
                        header.put("description", getTextValue(headerNode, "description"));
                        header.put("type", getTextValue(headerNode, "schema", "type"));
                        headers.add(header);
                    }
                }
                response.put("headers", headers);
                response.put("schema", getSchemaType(null));

                // 处理schema
                if (responseNode.has("content")) {
                    JsonNode contentNode = responseNode.get("content");
                    Iterator<Map.Entry<String, JsonNode>> contentFields = contentNode.fields();
                    if (contentFields.hasNext()) {
                        Map.Entry<String, JsonNode> contentEntry = contentFields.next();
                        JsonNode mediaTypeNode = contentEntry.getValue();
                        if (mediaTypeNode.has("schema")) {
                            response.put("schema", getSchemaType(mediaTypeNode.get("schema")));
                        }
                    }
                }
                responses.add(response);
            }
        }
        endpoint.put("responses", responses);
    }

    /**
     * 解析数据模型定义
     */
    private void parseDefinitions(JsonNode rootNode, Map<String, Object> dataModel) throws JsonProcessingException {
        List<Map<String, Object>> definitions = new ArrayList<>();
        if (rootNode.has("components") && rootNode.get("components").has("schemas")) {
            JsonNode schemasNode = rootNode.get("components").get("schemas");
            Iterator<Map.Entry<String, JsonNode>> schemaFields = schemasNode.fields();

            while (schemaFields.hasNext()) {
                Map.Entry<String, JsonNode> schemaEntry = schemaFields.next();
                String schemaName = schemaEntry.getKey();
                JsonNode schemaNode = schemaEntry.getValue();

                Map<String, Object> definition = new HashMap<>();
                definition.put("name", new BookmarkTextRenderData(schemaName, schemaName));

                // 处理属性
                List<Map<String, Object>> properties = new ArrayList<>();
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

                        Map<String, Object> property = new HashMap<>();
                        property.put("name", propertyName);
                        property.put("description", getTextValue(propertyNode, "description"));
                        property.put("required", requiredProps.contains(propertyName));
                        property.put("schema", getSchemaType(propertyNode));

                        properties.add(property);
                    }
                }
                definition.put("properties", properties);

                // 生成示例代码
                definition.put("definitionCode", generateExampleJson(schemaNode, schemasNode));
                definitions.add(definition);
            }
        }

        dataModel.put("definitions", definitions);
    }

    /**
     * 辅助方法
     */
    private Map<String, Object> findResourceByName(List<Map<String, Object>> resources, String name) {
        for (Map<String, Object> resource : resources) {
            if (name.equals(resource.get("name"))) {
                return resource;
            }
        }
        return null;
    }

    private boolean checkHasProduce(List<String> produces, String next) {
        if (produces == null || produces.isEmpty()) {
            return false;
        }
        return produces.contains(next);
    }

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
} 