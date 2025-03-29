package com.tools.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tools.model.ApiDataModel;
import java.util.Map;

/**
 * Interface for parsing OpenAPI documents and building data models
 */
public interface OpenApiParser {
    
    /**
     * Build a data model from an OpenAPI document
     * 
     * @param openApiContent The content of the OpenAPI document
     * @return A map containing the data model
     * @throws JsonProcessingException if there is an error processing the JSON
     */
    Map<String, Object> buildDataModel(String openApiContent) throws JsonProcessingException;
    
    /**
     * Build a structured data model from an OpenAPI document
     * 
     * @param openApiContent The content of the OpenAPI document
     * @return An ApiDataModel containing the structured data model
     * @throws JsonProcessingException if there is an error processing the JSON
     */
    ApiDataModel buildStructuredDataModel(String openApiContent) throws JsonProcessingException;
} 