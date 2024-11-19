package com.spring.soft.usermanagement.intgeration;

import com.spring.soft.usermanagement.dto.Order;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OrderServiceClient {

    private final RestTemplate restTemplate;
    private static final String ORDER_SERVICE_URL = "http://order-management:8081/api/orders/user/";

    public OrderServiceClient(@Qualifier("orderServiceRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Order> getOrdersByUserId(Long userId) {
        String url = ORDER_SERVICE_URL + userId;
        ResponseEntity<List<Order>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }
}
