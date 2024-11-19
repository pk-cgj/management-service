package com.spring.soft.ordermanagement.intgeration;

import com.spring.soft.ordermanagement.dto.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private static final String USER_SERVICE_URL = "http://user-management:8080/api/admin/users/";

    public UserServiceClient(@Qualifier("userServiceRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public User getUserByUserId(Long userId) {
        String url = USER_SERVICE_URL + userId;
        ResponseEntity<User> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }
}
