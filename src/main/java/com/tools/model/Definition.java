package com.tools.model;

import com.deepoove.poi.data.BookmarkTextRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.tools.highight.HighlightRenderData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示API数据模型定义的实体类
 */
public class Definition {
    private BookmarkTextRenderData name;
    private List<Property> properties = new ArrayList<>();
    private HighlightRenderData definitionCode;

    public BookmarkTextRenderData getName() {
        return name;
    }

    public void setName(BookmarkTextRenderData name) {
        this.name = name;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public HighlightRenderData getDefinitionCode() {
        return definitionCode;
    }

    public void setDefinitionCode(HighlightRenderData definitionCode) {
        this.definitionCode = definitionCode;
    }

    public void addProperty(Property property) {
        if (properties == null) {
            properties = new ArrayList<>();
        }
        properties.add(property);
    }

    /**
     * 转换为Map表示形式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        
        List<Map<String, Object>> propertyMaps = new ArrayList<>();
        if (properties != null) {
            for (Property property : properties) {
                propertyMaps.add(property.toMap());
            }
        }
        map.put("properties", propertyMaps);
        
        map.put("definitionCode", definitionCode);
        return map;
    }

    /**
     * 从Map构建实例
     */
    @SuppressWarnings("unchecked")
    public static Definition fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Definition definition = new Definition();
        definition.setName((BookmarkTextRenderData) map.get("name"));
        definition.setDefinitionCode((HighlightRenderData) map.get("definitionCode"));
        
        List<Map<String, Object>> propertyMaps = (List<Map<String, Object>>) map.get("properties");
        if (propertyMaps != null) {
            List<Property> properties = new ArrayList<>();
            for (Map<String, Object> propertyMap : propertyMaps) {
                properties.add(Property.fromMap(propertyMap));
            }
            definition.setProperties(properties);
        }
        
        return definition;
    }

    /**
     * 表示定义属性的实体类
     */
    public static class Property {
        private String name;
        private String description;
        private boolean required;
        private List<TextRenderData> schema;

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
        public static Property fromMap(Map<String, Object> map) {
            if (map == null) {
                return null;
            }

            Property property = new Property();
            property.setName((String) map.get("name"));
            property.setDescription((String) map.get("description"));
            property.setRequired((Boolean) map.getOrDefault("required", false));
            property.setSchema((List<TextRenderData>) map.get("schema"));
            
            return property;
        }
    }
} 