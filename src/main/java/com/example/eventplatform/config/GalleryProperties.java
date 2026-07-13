package com.example.eventplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gallery")
public record GalleryProperties(
        String storagePath
) {
}
