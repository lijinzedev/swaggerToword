package com.tools.services;

import com.deepoove.poi.data.BookmarkTextRenderData;
import com.deepoove.poi.data.HyperlinkTextRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.highight.HighlightRenderData;
import com.tools.highight.HighlightStyle;
import com.tools.model.*;
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
        // 为了保持接口兼容性，使用结构化实体方法，然后转换为Map
        ApiDataModel structuredModel = buildStructuredDataModel(openApiContent);
        return structuredModel.toMap();
    }

    @Override
    public ApiDataModel buildStructuredDataModel(String openApiContent) throws JsonProcessingException {
        OpenAPI openAPI = new OpenAPIV3Parser().readContents(openApiContent).getOpenAPI();
        return buildStructuredDataModel(openAPI);
    }

    /**
     * 构建结构化数据模型
     */
    public ApiDataModel buildStructuredDataModel(OpenAPI openAPI) throws JsonProcessingException {
        ApiDataModel dataModel = new ApiDataModel();

        // 解析info部分
        dataModel.setInfo(parseApiInfo(openAPI));

        // 解析paths部分 (接口列表)
        List<ApiResource> resources = parseApiResources(openAPI);
        for (ApiResource resource : resources) {
            dataModel.addResource(resource);
        }

        // 解析components/schemas部分 (数据模型)
        List<Definition> definitions = parseDefinitions(openAPI);
        for (Definition definition : definitions) {
            dataModel.addDefinition(definition);
        }

        return dataModel;
    }

    /**
     * 解析API信息
     */
    private ApiInfo parseApiInfo(OpenAPI openAPI) {
        Info apiInfo = openAPI.getInfo();
        ApiInfo info = new ApiInfo();
        
        if (apiInfo != null) {
            info.setTitle(apiInfo.getTitle() != null ? apiInfo.getTitle() : "");
            info.setDescription(apiInfo.getDescription() != null ? apiInfo.getDescription() : "");
            info.setVersion(apiInfo.getVersion() != null ? apiInfo.getVersion() : "");

            // 解析联系人信息
            if (apiInfo.getContact() != null) {
                Contact apiContact = apiInfo.getContact();
                ApiInfo.ContactInfo contact = new ApiInfo.ContactInfo();
                contact.setEmail(apiContact.getEmail() != null ? apiContact.getEmail() : "");
                info.setContact(contact);
            }

            // 解析许可证信息
            if (apiInfo.getLicense() != null) {
                License apiLicense = apiInfo.getLicense();
                ApiInfo.LicenseInfo license = new ApiInfo.LicenseInfo();
                license.setName(apiLicense.getName() != null ? apiLicense.getName() : "");
                info.setLicense(license);
            }
        }

        return info;
    }

    /**
     * 解析API资源路径
     */
    private List<ApiResource> parseApiResources(OpenAPI openAPI) {
        Map<String, ApiResource> resourceMap = new HashMap<>();
        Paths paths = openAPI.getPaths();

        if (paths != null) {
            for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();

                // 处理每个HTTP方法
                parseOperations(resourceMap, path, pathItem);
            }
        }

        return new ArrayList<>(resourceMap.values());
    }

    /**
     * 解析API操作
     */
    private void parseOperations(Map<String, ApiResource> resourceMap, String path, PathItem pathItem) {
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
            ApiResource resource = resourceMap.get(tag);
            if (resource == null) {
                resource = new ApiResource();
                resource.setName(tag);
                resource.setDescription("");
                resourceMap.put(tag, resource);
            }

            // 创建endpoint
            Endpoint endpoint = createEndpoint(path, httpMethod.name(), operation);

            // 添加endpoint到resource
            resource.addEndpoint(endpoint);
        }
    }

    /**
     * 创建端点信息
     */
    private Endpoint createEndpoint(String path, String httpMethod, Operation operation) {
        Endpoint endpoint = new Endpoint();
        endpoint.setSummary(operation.getSummary() != null ? operation.getSummary() : "");
        endpoint.setDescription(operation.getDescription() != null ? operation.getDescription() : "");
        endpoint.setHttpMethod(httpMethod.toUpperCase());
        endpoint.setUrl(path);

        // 处理produces (Content-Type)
        List<String> produces = parseProduces(operation);
        endpoint.setProduces(produces);

        // 处理consumes (请求Content-Type)
        List<String> consumes = parseConsumes(operation);
        endpoint.setConsumes(consumes);

        // 处理参数
        List<com.tools.model.Parameter> params = parseParameters(operation);
        endpoint.setParameters(params);

        // 处理响应
        List<Response> responses = parseResponses(operation);
        endpoint.setResponses(responses);

        return endpoint;
    }

    /**
     * 解析产生的内容类型
     */
    private List<String> parseProduces(Operation operation) {
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

        return produces;
    }

    /**
     * 解析消费的内容类型
     */
    private List<String> parseConsumes(Operation operation) {
        List<String> consumes = new ArrayList<>();
        
        RequestBody requestBody = operation.getRequestBody();
        if (requestBody != null && requestBody.getContent() != null) {
            consumes.addAll(requestBody.getContent().keySet());
        }
        
        return consumes;
    }

    /**
     * 解析参数
     */
    private List<com.tools.model.Parameter> parseParameters(Operation operation) {
        List<com.tools.model.Parameter> parameters = new ArrayList<>();

        // 处理常规参数
        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                com.tools.model.Parameter parameter = new com.tools.model.Parameter();
                parameter.setIn(param.getIn());
                parameter.setName(param.getName());
                parameter.setDescription(param.getDescription() != null ? param.getDescription() : "");
                parameter.setRequired(param.getRequired() != null ? param.getRequired() : false);

                // 处理schema
                if (param.getSchema() != null) {
                    parameter.setSchema(getSchemaType(param.getSchema()));
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
                com.tools.model.Parameter parameter = new com.tools.model.Parameter();
                parameter.setIn("body");
                parameter.setName("body");
                parameter.setDescription(requestBody.getDescription() != null ? requestBody.getDescription() : "");
                parameter.setRequired(requestBody.getRequired() != null ? requestBody.getRequired() : false);
                parameter.setSchema(getSchemaType(mediaType.getSchema()));
                parameters.add(parameter);
            }
        }

        return parameters;
    }

    /**
     * 解析响应
     */
    private List<Response> parseResponses(Operation operation) {
        List<Response> responsesList = new ArrayList<>();
        
        ApiResponses apiResponses = operation.getResponses();
        if (apiResponses != null) {
            for (Map.Entry<String, ApiResponse> responseEntry : apiResponses.entrySet()) {
                String code = responseEntry.getKey();
                ApiResponse apiResponse = responseEntry.getValue();
                
                Response response = new Response();
                response.setCode(code);
                response.setDescription(apiResponse.getDescription() != null ? apiResponse.getDescription() : "");

                // 处理headers
                if (apiResponse.getHeaders() != null) {
                    for (Map.Entry<String, io.swagger.v3.oas.models.headers.Header> headerEntry : apiResponse.getHeaders().entrySet()) {
                        String headerName = headerEntry.getKey();
                        io.swagger.v3.oas.models.headers.Header header = headerEntry.getValue();
                        
                        Response.Header headerObj = new Response.Header();
                        headerObj.setName(headerName);
                        headerObj.setDescription(header.getDescription() != null ? header.getDescription() : "");
                        
                        String type = "";
                        if (header.getSchema() != null && header.getSchema().getType() != null) {
                            type = header.getSchema().getType();
                        }
                        headerObj.setType(type);
                        
                        response.addHeader(headerObj);
                    }
                }

                // 处理schema
                if (apiResponse.getContent() != null && !apiResponse.getContent().isEmpty()) {
                    // 获取第一个内容类型的MediaType
                    Map.Entry<String, MediaType> entry = apiResponse.getContent().entrySet().iterator().next();
                    MediaType mediaType = entry.getValue();
                    
                    if (mediaType.getSchema() != null) {
                        response.setSchema(getSchemaType(mediaType.getSchema()));
                    }
                } else {
                    response.setSchema(new ArrayList<>());
                }
                
                responsesList.add(response);
            }
        }
        
        return responsesList;
    }

    /**
     * 解析数据模型定义
     */
    private List<Definition> parseDefinitions(OpenAPI openAPI) throws JsonProcessingException {
        List<Definition> definitions = new ArrayList<>();
        
        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            Map<String, Schema> schemas = openAPI.getComponents().getSchemas();
            
            for (Map.Entry<String, Schema> schemaEntry : schemas.entrySet()) {
                String schemaName = schemaEntry.getKey();
                Schema schema = schemaEntry.getValue();
                
                Definition definition = new Definition();
                definition.setName(new BookmarkTextRenderData(schemaName, schemaName));

                // 处理属性
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
                        
                        Definition.Property property = new Definition.Property();
                        property.setName(propertyName);
                        property.setDescription(propertySchema.getDescription() != null ? propertySchema.getDescription() : "");
                        property.setRequired(requiredProps.contains(propertyName));
                        property.setSchema(getSchemaType(propertySchema));
                        
                        definition.addProperty(property);
                    }
                }

                // 生成示例代码
                definition.setDefinitionCode(generateExampleJson(schema, schemas));
                
                definitions.add(definition);
            }
        }
        
        return definitions;
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