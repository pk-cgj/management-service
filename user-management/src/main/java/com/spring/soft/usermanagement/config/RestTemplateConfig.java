package com.spring.soft.usermanagement.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    @Qualifier("orderServiceRestTemplate")
    RestTemplate orderServiceRestTemplate(OAuth2AuthorizedClientManager authorizedClientManager) {
        return new RestTemplateBuilder()
                .interceptors((httpRequest, bytes, execution) -> {
                    OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                            .withClientRegistrationId("order-service")
                            .principal("user-service")
                            .build();
                    OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);

                    httpRequest.getHeaders().setBearerAuth(client.getAccessToken().getTokenValue());
                    return execution.execute(httpRequest, bytes);
                })
                .build();
    }
}