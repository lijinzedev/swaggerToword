package com.tools.model;

import com.deepoove.poi.data.TextRenderData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示API响应的实体类
 */
public class Response {
    private String code;
    private String description;
    private List<Header> headers = new ArrayList<>();
    private List<TextRenderData> schema;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public List<TextRenderData> getSchema() {
        return schema;
    }

    public void setSchema(List<TextRenderData> schema) {
        this.schema = schema;
    }

    public void addHeader(Header header) {
        if (headers == null) {
            headers = new ArrayList<>();
        }
        headers.add(header);
    }

    /**
     * 转换为Map表示形式
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("description", description);
        
        List<Map<String, Object>> headerMaps = new ArrayList<>();
        if (headers != null) {
            for (Header header : headers) {
                headerMaps.add(header.toMap());
            }
        }
        map.put("headers", headerMaps);
        
        map.put("schema", schema);
        return map;
    }

    /**
     * 从Map构建实例
     */
    @SuppressWarnings("unchecked")
    public static Response fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Response response = new Response();
        response.setCode((String) map.get("code"));
        response.setDescription((String) map.get("description"));
        
        List<Map<String, Object>> headerMaps = (List<Map<String, Object>>) map.get("headers");
        if (headerMaps != null) {
            List<Header> headers = new ArrayList<>();
            for (Map<String, Object> headerMap : headerMaps) {
                headers.add(Header.fromMap(headerMap));
            }
            response.setHeaders(headers);
        }
        
        response.setSchema((List<TextRenderData>) map.get("schema"));
        
        return response;
    }

    /**
     * 表示响应头的实体类
     */
    public static class Header {
        private String name;
        private String description;
        private String type;

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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        /**
         * 转换为Map表示形式
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("description", description);
            map.put("type", type);
            return map;
        }

        /**
         * 从Map构建实例
         */
        public static Header fromMap(Map<String, Object> map) {
            if (map == null) {
                return null;
            }

            Header header = new Header();
            header.setName((String) map.get("name"));
            header.setDescription((String) map.get("description"));
            header.setType((String) map.get("type"));
            
            return header;
        }
    }
} 