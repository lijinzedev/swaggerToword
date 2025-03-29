package com.tools.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示完整API数据模型的实体类
 */
public class ApiDataModel {
    private ApiInfo info;
    private List<ApiResource> resources = new ArrayList<>();
    private List<Definition> definitions = new ArrayList<>();

    public ApiInfo getInfo() {
        return info;
    }

    public void setInfo(ApiInfo info) {
        this.info = info;
    }

    public List<ApiResource> getResources() {
        return resources;
    }

    public void setResources(List<ApiResource> resources) {
        this.resources = resources;
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
    }

    public void addResource(ApiResource resource) {
        if (resources == null) {
            resources = new ArrayList<>();
        }
        resources.add(resource);
    }

    public void addDefinition(Definition definition) {
        if (definitions == null) {
            definitions = new ArrayList<>();
        }
        definitions.add(definition);
    }

    /**
     * 转换为Map表示形式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> dataModel = new HashMap<>();
        
        // 添加API信息
        if (info != null) {
            dataModel.put("info", info.toMap());
        }
        
        // 添加资源列表
        List<Map<String, Object>> resourceMaps = new ArrayList<>();
        if (resources != null) {
            for (ApiResource resource : resources) {
                resourceMaps.add(resource.toMap());
            }
        }
        dataModel.put("resources", resourceMaps);
        
        // 添加定义列表
        List<Map<String, Object>> definitionMaps = new ArrayList<>();
        if (definitions != null) {
            for (Definition definition : definitions) {
                definitionMaps.add(definition.toMap());
            }
        }
        dataModel.put("definitions", definitionMaps);
        
        return dataModel;
    }

    /**
     * 从Map构建实例
     */
    @SuppressWarnings("unchecked")
    public static ApiDataModel fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        ApiDataModel dataModel = new ApiDataModel();
        
        // 解析API信息
        Map<String, Object> infoMap = (Map<String, Object>) map.get("info");
        if (infoMap != null) {
            dataModel.setInfo(ApiInfo.fromMap(infoMap));
        }
        
        // 解析资源列表
        List<Map<String, Object>> resourceMaps = (List<Map<String, Object>>) map.get("resources");
        if (resourceMaps != null) {
            List<ApiResource> resources = new ArrayList<>();
            for (Map<String, Object> resourceMap : resourceMaps) {
                resources.add(ApiResource.fromMap(resourceMap));
            }
            dataModel.setResources(resources);
        }
        
        // 解析定义列表
        List<Map<String, Object>> definitionMaps = (List<Map<String, Object>>) map.get("definitions");
        if (definitionMaps != null) {
            List<Definition> definitions = new ArrayList<>();
            for (Map<String, Object> definitionMap : definitionMaps) {
                definitions.add(Definition.fromMap(definitionMap));
            }
            dataModel.setDefinitions(definitions);
        }
        
        return dataModel;
    }
} 