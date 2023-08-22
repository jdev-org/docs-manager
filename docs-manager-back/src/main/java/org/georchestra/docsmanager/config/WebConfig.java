package org.georchestra.docsmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.cors.pattern:/**}")
    String corsPathPattern;
    @Value("${spring.cors.origins:*}")
    String allowedOriginsUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping(corsPathPattern)
            .allowedOrigins(allowedOriginsUrl)
            .allowedMethods("GET", "POST", "PUT", "OPTIONS","DELETE");
    }
}
