package com.java.sms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.io.File;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir; // e.g. src/main/resources/static/uploads/profileImages/

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convert relative path to absolute and normalize separators
        String absolute = Paths.get(uploadDir).toAbsolutePath().toString().replace("\\", "/");
        if (!absolute.endsWith("/")) absolute = absolute + "/";

        // Log so you can verify where it's serving from
        System.out.println("StaticResourceConfig - serving files from: file:" + absolute);

        registry.addResourceHandler("/uploads/profileImages/**")
                .addResourceLocations("file:" + absolute);
    }
}
