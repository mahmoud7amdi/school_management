package com.smartedu.school_management_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Signing/expiry settings for issued JWTs, bound from {@code app.jwt.*}. */
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {

    /** HMAC-SHA256 signing secret. Must be at least 32 characters (256 bits). */
    private String secret;

    /** How long an issued token stays valid. */
    private Duration expiration = Duration.ofHours(10);

    /** Value placed in the {@code iss} claim. */
    private String issuer = "school-management-api";
}
