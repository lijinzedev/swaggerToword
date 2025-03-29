package com.tools.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示API资源的实体类
 */
public class ApiResource {
    private String name;
    private String description;
    private List<Endpoint> endpoints = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * 添加端点
     */
    public void addEndpoint(Endpoint endpoint) {
        if (endpoints == null) {
            endpoints = new ArrayList<>();
        }
        endpoints.add(endpoint);
    }

    /**
     * 转换为Map表示形式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("description", description);
        
        List<Map<String, Object>> endpointMaps = new ArrayList<>();
        if (endpoints != null) {
            for (Endpoint endpoint : endpoints) {
                endpointMaps.add(endpoint.toMap());
            }
        }
        map.put("endpoints", endpointMaps);
        
        return map;
    }

    /**
     * 从Map构建实例
     */
    @SuppressWarnings("unchecked")
    public static ApiResource fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        ApiResource resource = new ApiResource();
        resource.setName((String) map.get("name"));
        resource.setDescription((String) map.get("description"));
        
        List<Map<String, Object>> endpointMaps = (List<Map<String, Object>>) map.get("endpoints");
        if (endpointMaps != null) {
            List<Endpoint> endpoints = new ArrayList<>();
            for (Map<String, Object> endpointMap : endpointMaps) {
                endpoints.add(Endpoint.fromMap(endpointMap));
            }
            resource.setEndpoints(endpoints);
        }
        
        return resource;
    }
} 