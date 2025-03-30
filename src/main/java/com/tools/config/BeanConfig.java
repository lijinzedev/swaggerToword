package com.tools.config;

import com.tools.services.document.DatabaseDocumentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application bean configuration
 */
@Configuration
public class BeanConfig {
    
    /**
     * Database document service bean
     */
    @Bean
    public DatabaseDocumentService databaseDocumentService() {
        return new DatabaseDocumentService();
    }
} 