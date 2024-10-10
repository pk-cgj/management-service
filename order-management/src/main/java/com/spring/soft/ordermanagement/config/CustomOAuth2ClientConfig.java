package com.spring.soft.ordermanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CustomOAuth2ClientConfig {
    private final CustomProperties customProperties;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
        List<ClientRegistration> registrations = new ArrayList<>();

        properties.getRegistration().forEach((key, value) -> {
            ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(key);
            builder.clientId(value.getClientId());
            builder.clientSecret(value.getClientSecret());
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
            builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
            builder.redirectUri(value.getRedirectUri());
            builder.scope(value.getScope());

            // Keep authorization-uri as is (localhost:8888)
            builder.authorizationUri(properties.getProvider().get(key).getAuthorizationUri());

            // Modify these URLs for server-to-server communication
            builder.tokenUri(properties.getProvider().get(key).getTokenUri().replace(customProperties.getKeycloakExternalUri(), customProperties.getKeycloakInternalUri()));
            builder.jwkSetUri(properties.getProvider().get(key).getJwkSetUri().replace(customProperties.getKeycloakExternalUri(), customProperties.getKeycloakInternalUri()));
            builder.userInfoUri(properties.getProvider().get(key).getUserInfoUri().replace(customProperties.getKeycloakExternalUri(), customProperties.getKeycloakInternalUri()));

            // Modify issuer-uri for server-to-server communication
            String issuerUri = properties.getProvider().get(key).getIssuerUri().replace(customProperties.getKeycloakExternalUri(), customProperties.getKeycloakInternalUri());
            builder.providerConfigurationMetadata(java.util.Map.of("issuer", issuerUri));

            builder.userNameAttributeName(properties.getProvider().get(key).getUserNameAttribute());
            builder.clientName(key);

            registrations.add(builder.build());
        });

        return new InMemoryClientRegistrationRepository(registrations);
    }
}
