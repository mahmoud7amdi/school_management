package com.smartedu.school_management_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bootstrap super-admin account, bound from {@code app.admin.*}. */
@ConfigurationProperties(prefix = "app.admin")
@Getter
@Setter
public class AdminProperties {

    private String username = "admin";
    private String password = "admin123";
    private String email = "admin@smartedu.com";
    private String fullName = "System Administrator";
}
