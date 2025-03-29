package com.tools.model;

import com.deepoove.poi.data.TextRenderData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示API参数的实体类
 */
public class Parameter {
    private String in;
    private String name;
    private String description;
    private boolean required;
    private List<TextRenderData> schema;

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<TextRenderData> getSchema() {
        return schema;
    }

    public void setSchema(List<TextRenderData> schema) {
        this.schema = schema;
    }

    /**
     * 转换为Map表示形式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("in", in);
        map.put("name", name);
        map.put("description", description);
        map.put("required", required);
        map.put("schema", schema);
        return map;
    }

    /**
     * 从Map构建实例
     */
    @SuppressWarnings("unchecked")
    public static Parameter fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Parameter parameter = new Parameter();
        parameter.setIn((String) map.get("in"));
        parameter.setName((String) map.get("name"));
        parameter.setDescription((String) map.get("description"));
        parameter.setRequired((Boolean) map.getOrDefault("required", false));
        parameter.setSchema((List<TextRenderData>) map.get("schema"));
        
        return parameter;
    }
} 