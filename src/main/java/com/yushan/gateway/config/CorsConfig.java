package com.yushan.gateway.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CorsConfig {
    // CORS configuration moved to application.yml to avoid duplicate headers
    // The globalcors configuration in application.yml handles CORS
}
