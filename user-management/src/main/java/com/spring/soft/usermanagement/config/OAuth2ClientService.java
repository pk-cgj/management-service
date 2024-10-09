package com.spring.soft.usermanagement.config;

import com.spring.soft.usermanagement.exception.OAuth2ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class OAuth2ClientService {

    private final String orderServiceRegistrationId = System.getenv("INTERNAL_CLIENT_ID");
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestTemplate restTemplate;

    public OAuth2ClientService(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
        this.restTemplate = new RestTemplate();
    }

    public ResponseEntity<String> callProtectedApi(String url, HttpMethod method, Map<String, String> body) {
        try {
            log.debug("Attempting to authorize client with registration ID: {}", orderServiceRegistrationId);
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(orderServiceRegistrationId)
                    .principal(orderServiceRegistrationId)
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
            if (authorizedClient == null) {
                log.error("Failed to obtain OAuth2 token. Authorized client is null.");
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, "Unable to authorize client", null);
                throw new OAuth2AuthorizationException(error);
            }

            log.debug("Successfully obtained OAuth2 token for client: {}", orderServiceRegistrationId);
            log.debug("Access token: {}", authorizedClient.getAccessToken().getTokenValue());
            log.debug("Token type: {}", authorizedClient.getAccessToken().getTokenType().getValue());
            log.debug("Expires at: {}", authorizedClient.getAccessToken().getExpiresAt());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> request;
            if (body != null && (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {
                request = new HttpEntity<>(body, headers);
            } else {
                request = new HttpEntity<>(headers);
            }

            log.debug("Calling protected API at URL: {} with method: {}", url, method);
            ResponseEntity<String> response = restTemplate.exchange(url, method, request, String.class);
            log.debug("Received response from protected API. Status code: {}", response.getStatusCode());
            return response;

        } catch (OAuth2AuthorizationException e) {
            log.error("OAuth2 authorization failed", e);
            throw new OAuth2ClientException("Failed to obtain OAuth2 token", e);
        } catch (HttpClientErrorException e) {
            log.error("Client error when calling protected API: {}", e.getStatusCode(), e);
            throw new OAuth2ClientException("Client error when calling protected API: " + e.getStatusCode(), e);
        } catch (HttpServerErrorException e) {
            log.error("Server error when calling protected API: {}", e.getStatusCode(), e);
            throw new OAuth2ClientException("Server error when calling protected API: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            log.error("Error occurred while calling protected API", e);
            throw new OAuth2ClientException("Error occurred while calling protected API", e);
        }
    }

    public ResponseEntity<String> get(String url) {
        return callProtectedApi(url, HttpMethod.GET, null);
    }

    public ResponseEntity<String> post(String url, Map<String, String> body) {
        return callProtectedApi(url, HttpMethod.POST, body);
    }

    public ResponseEntity<String> put(String url, Map<String, String> body) {
        return callProtectedApi(url, HttpMethod.PUT, body);
    }

    public ResponseEntity<String> delete(String url) {
        return callProtectedApi(url, HttpMethod.DELETE, null);
    }
}
