package com.smartedu.school_management_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where uploaded files live, bound from {@code app.storage.*}.
 *
 * <p>Outside the jar by design: files written inside {@code target/} would not survive a
 * rebuild, and a packaged jar cannot be written to at all. The default is a directory
 * beside the application, and every real deployment should point this at a volume that is
 * included in backups — the database alone will not carry these images.
 */
@ConfigurationProperties(prefix = "app.storage")
@Getter
@Setter
public class StorageProperties {

    /** Root for uploads; avatars go in an {@code avatars} subdirectory. */
    private String uploadDir = "uploads";

    /** Rejected above this size, before anything is written. */
    private long maxAvatarBytes = 2L * 1024 * 1024;
}
