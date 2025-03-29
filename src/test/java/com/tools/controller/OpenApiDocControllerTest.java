package com.tools.controller;

import com.tools.services.OpenApiDocService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class OpenApiDocControllerTest {

    @Mock
    private OpenApiDocService openApiDocService;

    @InjectMocks
    private OpenApiDocController controller;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testGenerateFromUrl() throws Exception {
        // Sample test data
        String openApiUrl = "https://petstore.swagger.io/v2/swagger.json";
        String templateName = null;
        byte[] docBytes = "sample doc content".getBytes(StandardCharsets.UTF_8);

        // Mock service behavior
        when(openApiDocService.generateDocFromUrl(eq(openApiUrl), eq(templateName))).thenReturn(docBytes);

        // Execute and verify
        mockMvc.perform(post("/api/openapi-doc/generate-from-url")
                .param("url", openApiUrl))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"openapi-doc.docx\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(docBytes));
    }

    @Test
    public void testGenerateFromUrlWithCustomTemplate() throws Exception {
        // Sample test data
        String openApiUrl = "https://petstore.swagger.io/v2/swagger.json";
        byte[] docBytes = "sample doc content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile templateFile = new MockMultipartFile(
                "template", 
                "custom-template.docx", 
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
                "template content".getBytes(StandardCharsets.UTF_8));

        // Mock service behavior
        when(openApiDocService.generateDocFromUrl(eq(openApiUrl), any(InputStream.class))).thenReturn(docBytes);

        // Execute and verify
        mockMvc.perform(multipart("/api/openapi-doc/generate-from-url")
                .file(templateFile)
                .param("url", openApiUrl))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"openapi-doc.docx\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(docBytes));
    }

    @Test
    public void testGenerateFromJson() throws Exception {
        // Sample test data
        String openApiJson = "{ \"openapi\": \"3.0.0\", \"info\": { \"title\": \"Test API\", \"version\": \"1.0.0\" } }";
        String templateName = "default-template";
        byte[] docBytes = "sample doc content".getBytes(StandardCharsets.UTF_8);

        // Mock service behavior
        when(openApiDocService.generateDocFromJson(eq(openApiJson), eq(templateName))).thenReturn(docBytes);

        // Execute and verify
        mockMvc.perform(post("/api/openapi-doc/generate-from-json")
                .contentType(MediaType.APPLICATION_JSON)
                .content(openApiJson)
                .param("templateName", templateName))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"openapi-doc.docx\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(docBytes));
    }

    @Test
    public void testGenerateFromFile() throws Exception {
        // Sample test data
        String openApiJsonContent = "{ \"openapi\": \"3.0.0\", \"info\": { \"title\": \"Test API\", \"version\": \"1.0.0\" } }";
        String templateName = null;
        byte[] docBytes = "sample doc content".getBytes(StandardCharsets.UTF_8);
        
        MockMultipartFile jsonFile = new MockMultipartFile(
                "file", 
                "openapi.json", 
                MediaType.APPLICATION_JSON_VALUE, 
                openApiJsonContent.getBytes(StandardCharsets.UTF_8));

        // Mock service behavior
        when(openApiDocService.generateDocFromFile(any(InputStream.class), eq(templateName))).thenReturn(docBytes);

        // Execute and verify
        mockMvc.perform(multipart("/api/openapi-doc/generate-from-file")
                .file(jsonFile))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"openapi-doc.docx\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(docBytes));
    }
} 