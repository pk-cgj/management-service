package com.spring.soft.ordermanagement.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class RestTemplateConfig {
    @Bean
    @Qualifier("userServiceRestTemplate")
    RestTemplate userServiceRestTemplate(OAuth2AuthorizedClientManager authorizedClientManager) {
        return new RestTemplateBuilder()
                .interceptors((httpRequest, bytes, execution) -> {
                    OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                            .withClientRegistrationId("user-service")
                            .principal("order-service")
                            .build();
                    OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);

                    httpRequest.getHeaders().setBearerAuth(client.getAccessToken().getTokenValue());
                    return execution.execute(httpRequest, bytes);
                })
                .build();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                    // Map scopes to authorities
                    if (userInfo != null && idToken != null) {
                        List<String> scopes = idToken.getClaim("scope");
                        if (scopes != null) {
                            scopes.forEach(scope ->
                                    mappedAuthorities.add(new SimpleGrantedAuthority(scope))
                            );
                        }
                    }
                }
            });
            return mappedAuthorities;
        };
    }
}