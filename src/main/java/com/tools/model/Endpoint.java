package com.tools.model;

import com.deepoove.poi.data.TextRenderData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示API端点的实体类
 */
public class Endpoint {
    private String summary;
    private String description;
    private String httpMethod;
    private String url;
    private List<String> produces = new ArrayList<>();
    private List<String> consumes = new ArrayList<>();
    private List<Parameter> parameters = new ArrayList<>();
    private List<Response> responses = new ArrayList<>();

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getProduces() {
        return produces;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public List<Response> getResponses() {
        return responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }

    public void addParameter(Parameter parameter) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        parameters.add(parameter);
    }

    public void addResponse(Response response) {
        if (responses == null) {
            responses = new ArrayList<>();
        }
        responses.add(response);
    }

    /**
     * 转换为Map表示形式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("summary", summary);
        map.put("description", description);
        map.put("httpMethod", httpMethod);
        map.put("url", url);
        map.put("produces", produces);
        map.put("consumes", consumes);
        
        List<Map<String, Object>> parameterMaps = new ArrayList<>();
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                parameterMaps.add(parameter.toMap());
            }
        }
        map.put("parameters", parameterMaps);
        
        List<Map<String, Object>> responseMaps = new ArrayList<>();
        if (responses != null) {
            for (Response response : responses) {
                responseMaps.add(response.toMap());
            }
        }
        map.put("responses", responseMaps);
        
        return map;
    }

    /**
     * 从Map构建实例
     */
    @SuppressWarnings("unchecked")
    public static Endpoint fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Endpoint endpoint = new Endpoint();
        endpoint.setSummary((String) map.get("summary"));
        endpoint.setDescription((String) map.get("description"));
        endpoint.setHttpMethod((String) map.get("httpMethod"));
        endpoint.setUrl((String) map.get("url"));
        
        List<String> produces = (List<String>) map.get("produces");
        if (produces != null) {
            endpoint.setProduces(produces);
        }
        
        List<String> consumes = (List<String>) map.get("consumes");
        if (consumes != null) {
            endpoint.setConsumes(consumes);
        }
        
        List<Map<String, Object>> parameterMaps = (List<Map<String, Object>>) map.get("parameters");
        if (parameterMaps != null) {
            List<Parameter> parameters = new ArrayList<>();
            for (Map<String, Object> parameterMap : parameterMaps) {
                parameters.add(Parameter.fromMap(parameterMap));
            }
            endpoint.setParameters(parameters);
        }
        
        List<Map<String, Object>> responseMaps = (List<Map<String, Object>>) map.get("responses");
        if (responseMaps != null) {
            List<Response> responses = new ArrayList<>();
            for (Map<String, Object> responseMap : responseMaps) {
                responses.add(Response.fromMap(responseMap));
            }
            endpoint.setResponses(responses);
        }
        
        return endpoint;
    }
} 