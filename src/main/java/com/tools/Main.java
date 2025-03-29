package com.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
    
    @Component
    public static class SwaggerUrlPrinter implements ApplicationRunner {
        
        private static final Logger logger = LoggerFactory.getLogger(SwaggerUrlPrinter.class);
        private final Environment environment;
        
        public SwaggerUrlPrinter(Environment environment) {
            this.environment = environment;
        }
        
        @Override
        public void run(ApplicationArguments args) throws Exception {
            String port = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            
            String separator = "\n----------------------------------------------------------";
            logger.info("{}", separator);
            logger.info("  应用启动成功!");
            logger.info("  Swagger文档: http://localhost:{}{}/swagger-ui.html", port, contextPath);
            logger.info("  外部访问文档: http://{}:{}{}/swagger-ui.html", hostAddress, port, contextPath);
            logger.info("{}", separator);
        }
    }
}