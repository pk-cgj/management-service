package com.spring.soft.usermanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "custom-properties")
@Data
public class CustomProperties {

    private String keycloakInternalUri;
    private String keycloakExternalUri;
    private String internalClientId;
    private String internalClientSecret;
    private String internalTokenUri;
}
