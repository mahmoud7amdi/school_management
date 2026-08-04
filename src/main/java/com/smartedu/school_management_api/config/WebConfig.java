package com.smartedu.school_management_api.config;

import com.smartedu.school_management_api.service.AvatarStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves uploaded files from the storage directory.
 *
 * <p>Uploads live outside the jar, so they are not on the classpath and the default
 * static handling does not see them. This maps the public URL onto the configured
 * directory; the trailing separator matters, without it Spring treats the location as a
 * file rather than a folder.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path avatars = Paths.get(storageProperties.getUploadDir(), "avatars")
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler(AvatarStorageService.PUBLIC_PREFIX + "**")
                .addResourceLocations(avatars.toUri().toString())
                // Short cache: the file name is stable per user, so a long max-age would
                // keep showing the old picture after a change. The client also appends a
                // cache-busting query when it knows the avatar just changed.
                .setCachePeriod(60);
    }
}
