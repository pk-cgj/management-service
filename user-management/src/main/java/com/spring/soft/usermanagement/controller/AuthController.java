package com.spring.soft.usermanagement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.soft.usermanagement.config.JwtTokenExtractor;
import com.spring.soft.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final JwtTokenExtractor jwtTokenExtractor;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenUri;

    @PostMapping("/login")
    public ResponseEntity<?> getToken(@RequestParam String username, @RequestParam String password) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("username", username);
        map.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                String accessToken = root.path("access_token").asText();
                String email = jwtTokenExtractor.extractEmailFromToken(accessToken);

                userService.createOrUpdateUserFromKeycloak(username, email);

                return ResponseEntity.ok(response.getBody());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return response;
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", jwt.getSubject());
        userInfo.put("preferred_username", jwt.getClaim("preferred_username"));
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("name", jwt.getClaim("name"));
        userInfo.put("scope", jwt.getClaim("scope"));
        return userInfo;
    }
}
