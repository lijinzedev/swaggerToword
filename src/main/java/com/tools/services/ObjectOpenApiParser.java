package com.tools.services;

import com.deepoove.poi.data.BookmarkTextRenderData;
import com.deepoove.poi.data.HyperlinkTextRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.highight.HighlightRenderData;
import com.tools.highight.HighlightStyle;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ObjectOpenApiParser implements OpenApiParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> buildDataModel(String openApiContent) throws JsonProcessingException {
        OpenAPI openAPI = new OpenAPIV3Parser().readContents(openApiContent).getOpenAPI();
        return buildDataModel(openAPI);
    }

    /**
     * 构建数据模型
     */
    public Map<String, Object> buildDataModel(OpenAPI openAPI) throws JsonProcessingException {
        Map<String, Object> dataModel = new HashMap<>();

        // 解析info部分
        parseInfo(openAPI, dataModel);

        // 解析paths部分 (接口列表)
        parseResources(openAPI, dataModel);

        // 解析components/schemas部分 (数据模型)
        parseDefinitions(openAPI, dataModel);

        return dataModel;
    }

    /**
     * 解析API信息
     */
    private void parseInfo(OpenAPI openAPI, Map<String, Object> dataModel) {
        Info apiInfo = openAPI.getInfo();
        Map<String, Object> info = new HashMap<>();
        
        if (apiInfo != null) {
            info.put("title", apiInfo.getTitle() != null ? apiInfo.getTitle() : "");
            info.put("description", apiInfo.getDescription() != null ? apiInfo.getDescription() : "");
            info.put("version", apiInfo.getVersion() != null ? apiInfo.getVersion() : "");

            // 解析联系人信息
            Map<String, String> contact = new HashMap<>();
            if (apiInfo.getContact() != null) {
                Contact apiContact = apiInfo.getContact();
                contact.put("email", apiContact.getEmail() != null ? apiContact.getEmail() : "");
            }
            info.put("contact", contact);

            // 解析许可证信息
            Map<String, String> license = new HashMap<>();
            if (apiInfo.getLicense() != null) {
                License apiLicense = apiInfo.getLicense();
                license.put("name", apiLicense.getName() != null ? apiLicense.getName() : "");
            }
            info.put("license", license);
        }

        dataModel.put("info", info);
    }

    /**
     * 解析API资源路径
     */
    private void parseResources(OpenAPI openAPI, Map<String, Object> dataModel) {
        List<Map<String, Object>> resources = new ArrayList<>();
        Paths paths = openAPI.getPaths();

        if (paths != null) {
            for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();

                // 处理每个HTTP方法
                parseOperations(resources, path, pathItem);
            }
        }

        dataModel.put("resources", resources);
    }

    /**
     * 解析API操作
     */
    private void parseOperations(List<Map<String, Object>> resources, String path, PathItem pathItem) {
        Map<PathItem.HttpMethod, Operation> operationsMap = new HashMap<>();
        
        if (pathItem.getGet() != null) operationsMap.put(PathItem.HttpMethod.GET, pathItem.getGet());
        if (pathItem.getPost() != null) operationsMap.put(PathItem.HttpMethod.POST, pathItem.getPost());
        if (pathItem.getPut() != null) operationsMap.put(PathItem.HttpMethod.PUT, pathItem.getPut());
        if (pathItem.getDelete() != null) operationsMap.put(PathItem.HttpMethod.DELETE, pathItem.getDelete());
        if (pathItem.getPatch() != null) operationsMap.put(PathItem.HttpMethod.PATCH, pathItem.getPatch());
        if (pathItem.getHead() != null) operationsMap.put(PathItem.HttpMethod.HEAD, pathItem.getHead());
        if (pathItem.getOptions() != null) operationsMap.put(PathItem.HttpMethod.OPTIONS, pathItem.getOptions());
        if (pathItem.getTrace() != null) operationsMap.put(PathItem.HttpMethod.TRACE, pathItem.getTrace());

        for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : operationsMap.entrySet()) {
            PathItem.HttpMethod httpMethod = operationEntry.getKey();
            Operation operation = operationEntry.getValue();

            // 找到对应的tag作为resource分组
            String tag = "Default";
            if (operation.getTags() != null && !operation.getTags().isEmpty()) {
                tag = operation.getTags().get(0);
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
            Map<String, Object> endpoint = createEndpoint(path, httpMethod.name(), operation);

            // 添加endpoint到resource
            ((List<Map<String, Object>>) resource.get("endpoints")).add(endpoint);
        }
    }

    /**
     * 创建端点信息
     */
    private Map<String, Object> createEndpoint(String path, String httpMethod, Operation operation) {
        Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("summary", operation.getSummary() != null ? operation.getSummary() : "");
        endpoint.put("description", operation.getDescription() != null ? operation.getDescription() : "");
        endpoint.put("httpMethod", httpMethod.toUpperCase());
        endpoint.put("url", path);

        // 处理produces (Content-Type)
        parseProduces(operation, endpoint);

        // 处理consumes (请求Content-Type)
        parseConsumes(operation, endpoint);

        // 处理参数
        parseParameters(operation, endpoint);

        // 处理响应
        parseResponses(operation, endpoint);

        return endpoint;
    }

    /**
     * 解析产生的内容类型
     */
    private void parseProduces(Operation operation, Map<String, Object> endpoint) {
        List<String> produces = new ArrayList<>();

        ApiResponses responses = operation.getResponses();
        if (responses != null) {
            for (Map.Entry<String, ApiResponse> responseEntry : responses.entrySet()) {
                ApiResponse response = responseEntry.getValue();
                Content content = response.getContent();
                
                if (content != null) {
                    for (String contentType : content.keySet()) {
                        if (!produces.contains(contentType)) {
                            produces.add(contentType);
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
    private void parseConsumes(Operation operation, Map<String, Object> endpoint) {
        List<String> consumes = new ArrayList<>();
        
        RequestBody requestBody = operation.getRequestBody();
        if (requestBody != null && requestBody.getContent() != null) {
            consumes.addAll(requestBody.getContent().keySet());
        }
        
        endpoint.put("consumes", consumes);
    }

    /**
     * 解析参数
     */
    private void parseParameters(Operation operation, Map<String, Object> endpoint) {
        List<Map<String, Object>> parameters = new ArrayList<>();

        // 处理常规参数
        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                Map<String, Object> parameter = new HashMap<>();
                parameter.put("in", param.getIn());
                parameter.put("name", param.getName());
                parameter.put("description", param.getDescription() != null ? param.getDescription() : "");
                parameter.put("required", param.getRequired() != null ? param.getRequired() : false);

                // 处理schema
                if (param.getSchema() != null) {
                    parameter.put("schema", getSchemaType(param.getSchema()));
                }
                parameters.add(parameter);
            }
        }

        // 处理requestBody参数
        RequestBody requestBody = operation.getRequestBody();
        if (requestBody != null && requestBody.getContent() != null && !requestBody.getContent().isEmpty()) {
            // 获取第一个内容类型的MediaType
            Map.Entry<String, MediaType> entry = requestBody.getContent().entrySet().iterator().next();
            MediaType mediaType = entry.getValue();
            
            if (mediaType.getSchema() != null) {
                Map<String, Object> parameter = new HashMap<>();
                parameter.put("in", "body");
                parameter.put("name", "body");
                parameter.put("description", requestBody.getDescription() != null ? requestBody.getDescription() : "");
                parameter.put("required", requestBody.getRequired() != null ? requestBody.getRequired() : false);
                parameter.put("schema", getSchemaType(mediaType.getSchema()));
                parameters.add(parameter);
            }
        }

        endpoint.put("parameters", parameters);
    }

    /**
     * 解析响应
     */
    private void parseResponses(Operation operation, Map<String, Object> endpoint) {
        List<Map<String, Object>> responses = new ArrayList<>();
        
        ApiResponses apiResponses = operation.getResponses();
        if (apiResponses != null) {
            for (Map.Entry<String, ApiResponse> responseEntry : apiResponses.entrySet()) {
                String code = responseEntry.getKey();
                ApiResponse apiResponse = responseEntry.getValue();
                
                Map<String, Object> response = new HashMap<>();
                response.put("code", code);
                response.put("description", apiResponse.getDescription() != null ? apiResponse.getDescription() : "");

                // 处理headers
                List<Map<String, Object>> headers = new ArrayList<>();
                if (apiResponse.getHeaders() != null) {
                    for (Map.Entry<String, io.swagger.v3.oas.models.headers.Header> headerEntry : apiResponse.getHeaders().entrySet()) {
                        String headerName = headerEntry.getKey();
                        io.swagger.v3.oas.models.headers.Header header = headerEntry.getValue();
                        
                        Map<String, Object> headerMap = new HashMap<>();
                        headerMap.put("name", headerName);
                        headerMap.put("description", header.getDescription() != null ? header.getDescription() : "");
                        
                        String type = "";
                        if (header.getSchema() != null && header.getSchema().getType() != null) {
                            type = header.getSchema().getType();
                        }
                        headerMap.put("type", type);
                        
                        headers.add(headerMap);
                    }
                }
                response.put("headers", headers);
                response.put("schema", getSchemaType(null));

                // 处理schema
                if (apiResponse.getContent() != null && !apiResponse.getContent().isEmpty()) {
                    // 获取第一个内容类型的MediaType
                    Map.Entry<String, MediaType> entry = apiResponse.getContent().entrySet().iterator().next();
                    MediaType mediaType = entry.getValue();
                    
                    if (mediaType.getSchema() != null) {
                        response.put("schema", getSchemaType(mediaType.getSchema()));
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
    private void parseDefinitions(OpenAPI openAPI, Map<String, Object> dataModel) throws JsonProcessingException {
        List<Map<String, Object>> definitions = new ArrayList<>();
        
        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
            
            for (Map.Entry<String, Schema> schemaEntry : schemas.entrySet()) {
                String schemaName = schemaEntry.getKey();
                Schema schema = schemaEntry.getValue();
                
                Map<String, Object> definition = new HashMap<>();
                definition.put("name", new BookmarkTextRenderData(schemaName, schemaName));

                // 处理属性
                List<Map<String, Object>> properties = new ArrayList<>();
                if (schema.getProperties() != null) {
                    // 获取required属性列表
                    List<String> requiredProps = schema.getRequired() != null ? schema.getRequired() : new ArrayList<>();
                    
                    Map<String, Schema> schemaProperties = new HashMap<>();
                    // Safely handle properties which might be returned as a raw map
                    Object propertiesObj = schema.getProperties();
                    if (propertiesObj instanceof Map) {
                        Map<?, ?> propertiesMap = (Map<?, ?>) propertiesObj;
                        for (Map.Entry<?, ?> entry : propertiesMap.entrySet()) {
                            if (entry.getKey() instanceof String && entry.getValue() instanceof Schema) {
                                schemaProperties.put((String) entry.getKey(), (Schema) entry.getValue());
                            }
                        }
                    }
                    
                    for (Map.Entry<String, Schema> propertyEntry : schemaProperties.entrySet()) {
                        String propertyName = propertyEntry.getKey();
                        Schema propertySchema = propertyEntry.getValue();
                        
                        Map<String, Object> property = new HashMap<>();
                        property.put("name", propertyName);
                        property.put("description", propertySchema.getDescription() != null ? propertySchema.getDescription() : "");
                        property.put("required", requiredProps.contains(propertyName));
                        property.put("schema", getSchemaType(propertySchema));
                        
                        properties.add(property);
                    }
                }
                definition.put("properties", properties);

                // 生成示例代码
                definition.put("definitionCode", generateExampleJson(schema, schemas));
                
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

    private List<TextRenderData> getSchemaType(Schema schema) {
        List<TextRenderData> result = new ArrayList<>();
        
        if (schema == null) {
            return result;
        }
        
        if (schema.get$ref() != null) {
            String ref = schema.get$ref();
            String refName = ref.substring(ref.lastIndexOf("/") + 1);
            result.add(new HyperlinkTextRenderData(refName, "anchor:" + refName));
            return result;
        }
        
        String type = schema.getType();
        
        if ("array".equals(type) && schema.getItems() != null) {
            result.add(new TextRenderData("<"));
            result.addAll(getSchemaType(schema.getItems()));
            result.add(new TextRenderData(">"));
            result.add(new TextRenderData("array"));
            return result;
        }
        
        if ("object".equals(type) && schema.getAdditionalProperties() != null) {
            result.add(new TextRenderData("map[string, "));
            Object additionalProps = schema.getAdditionalProperties();
            if (additionalProps instanceof Schema) {
                result.addAll(getSchemaType((Schema) additionalProps));
            } else {
                result.add(new TextRenderData("object"));
            }
            result.add(new TextRenderData("]"));
            return result;
        }
        
        result.add(new TextRenderData(type != null ? type : "object"));
        return result;
    }

    /**
     * 生成示例JSON
     */
    private HighlightRenderData generateExampleJson(Schema schema, Map<String, Schema> schemas) throws JsonProcessingException {
        Map<String, Object> exampleJson = generateExampleObject(schema, schemas, new HashSet<>());
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
    private Map<String, Object> generateExampleObject(Schema schema, Map<String, Schema> schemas, Set<String> processedRefs) {
        Map<String, Object> obj = new LinkedHashMap<>();

        if (schema.getProperties() != null) {
            // Safely handle properties which might be returned as a raw map
            Map<String, Schema> schemaProperties = new HashMap<>();
            Object propertiesObj = schema.getProperties();
            if (propertiesObj instanceof Map) {
                Map<?, ?> propertiesMap = (Map<?, ?>) propertiesObj;
                for (Map.Entry<?, ?> entry : propertiesMap.entrySet()) {
                    if (entry.getKey() instanceof String && entry.getValue() instanceof Schema) {
                        schemaProperties.put((String) entry.getKey(), (Schema) entry.getValue());
                    }
                }
            }
            
            for (Map.Entry<String, Schema> entry : schemaProperties.entrySet()) {
                obj.put(entry.getKey(), generateExampleValue(entry.getValue(), schemas, processedRefs));
            }
        } else if (schema.getAdditionalProperties() != null) {
            Object additionalProps = schema.getAdditionalProperties();
            if (additionalProps instanceof Schema) {
                obj.put("key", generateExampleValue((Schema) additionalProps, schemas, processedRefs));
            } else {
                obj.put("key", "example");
            }
        }

        return obj;
    }

    /**
     * 生成示例值
     */
    private Object generateExampleValue(Schema schema, Map<String, Schema> schemas, Set<String> processedRefs) {
        // 如果有example字段，优先使用
        if (schema.getExample() != null) {
            return schema.getExample().toString();
        }

        // 处理引用
        if (schema.get$ref() != null) {
            String ref = schema.get$ref();
            // 解析引用路径，支持OpenAPI 3.0格式的引用
            String[] refParts = ref.split("/");
            String refName = refParts[refParts.length - 1];

            // 避免循环引用
            if (processedRefs.contains(refName)) {
                return ref; // 返回引用路径而不是展开对象
            }

            processedRefs.add(refName);
            if (schemas.containsKey(refName)) {
                return generateExampleObject(schemas.get(refName), schemas, processedRefs);
            }
            return new LinkedHashMap<>(); // 如果找不到引用的定义
        }

        String type = schema.getType() != null ? schema.getType() : "object";

        switch (type) {
            case "string":
                // 如果有format字段，可以生成更合适的示例
                if (schema.getFormat() != null) {
                    String format = schema.getFormat();
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
                if (schema.getFormat() != null && "int64".equals(schema.getFormat())) {
                    return 10000000000L;
                }
                return 0;
            case "boolean":
                return false;
            case "array":
                List<Object> array = new ArrayList<>();
                if (schema.getItems() != null) {
                    array.add(generateExampleValue(schema.getItems(), schemas, processedRefs));
                }
                return array;
            case "object":
                return generateExampleObject(schema, schemas, processedRefs);
            default:
                return "unknown";
        }
    }
} 