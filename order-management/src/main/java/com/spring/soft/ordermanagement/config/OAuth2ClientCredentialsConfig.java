package com.spring.soft.ordermanagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class OAuth2ClientCredentialsConfig {
    private final ClientRegistrationRepository existingClientRegistrationRepository;

    @Primary
    @Bean
    public ClientRegistrationRepository extendedClientRegistrationRepository() {
        ClientRegistration userServiceRegistration = ClientRegistration.withRegistrationId(System.getenv("INTERNAL_CLIENT_ID"))
                .tokenUri(System.getenv("INTERNAL_TOKEN_URI"))
                .clientId(System.getenv("INTERNAL_CLIENT_ID"))
                .clientSecret(System.getenv("INTERNAL_CLIENT_SECRET"))
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("openid", "profile", "email")
                .build();

        log.info("Created user-service client registration with client ID: {}, token URI: {}, scopes: {}",
                userServiceRegistration.getClientId(),
                userServiceRegistration.getProviderDetails().getTokenUri(),
                userServiceRegistration.getScopes());

        ClientRegistration existingRegistration = existingClientRegistrationRepository.findByRegistrationId("keycloak");

        log.info("Created order-service client registration with client ID: {}, token URI: {}, scopes: {}",
                existingRegistration.getClientId(),
                existingRegistration.getProviderDetails().getTokenUri(),
                existingRegistration.getScopes());

        return new InMemoryClientRegistrationRepository(existingRegistration, userServiceRegistration);
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository extendedClientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(extendedClientRegistrationRepository);
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> accessTokenResponseClient =
                new LoggingOAuth2AccessTokenResponseClient(new DefaultClientCredentialsTokenResponseClient());

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials(configurer ->
                                configurer.accessTokenResponseClient(accessTokenResponseClient))
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Slf4j
    private static class LoggingOAuth2AccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

        private final OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> delegate;

        public LoggingOAuth2AccessTokenResponseClient(OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> delegate) {
            this.delegate = delegate;
        }

        @Override
        public OAuth2AccessTokenResponse getTokenResponse(OAuth2ClientCredentialsGrantRequest grantRequest) {
            ClientRegistration clientRegistration = grantRequest.getClientRegistration();
            log.debug("Preparing token request for client ID: {}", clientRegistration.getClientId());
            log.debug("Token URI: {}", clientRegistration.getProviderDetails().getTokenUri());
            log.debug("Grant type: {}", clientRegistration.getAuthorizationGrantType());
            log.debug("Scopes: {}", clientRegistration.getScopes());

            try {
                OAuth2AccessTokenResponse response = delegate.getTokenResponse(grantRequest);
                log.debug("Received token response for client ID: {}", clientRegistration.getClientId());
                log.debug("Access token: {}", response.getAccessToken().getTokenValue());
                log.debug("Token type: {}", response.getAccessToken().getTokenType().getValue());
                return response;
            } catch (OAuth2AuthorizationException e) {
                log.error("Error getting token for client ID: {}. Error: {}",
                        clientRegistration.getClientId(),
                        e.getError().getDescription(), e);
                throw e;
            }
        }
    }
}
