package com.fleetguard360.monitoring_service.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // cualquier ruta que NO sea estático ni /api/** → index.html
        registry.addViewController("/{spring:[\\w\\-]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{spring:[\\w\\-]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:[\\w\\-]+}/**{spring:[\\w\\-]+}")
                .setViewName("forward:/index.html");
    }
}
