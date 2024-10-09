package com.spring.soft.usermanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.soft.usermanagement.config.OAuth2ClientService;
import com.spring.soft.usermanagement.dto.UserDetailsDTO;
import com.spring.soft.usermanagement.entity.User;
import com.spring.soft.usermanagement.exception.OrderNotFoundException;
import com.spring.soft.usermanagement.exception.UserNotFoundException;
import com.spring.soft.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OAuth2ClientService oauth2ClientService;
    private final ObjectMapper objectMapper;

    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserDetailsDTO getUserDetailsById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Map<String, Object>> orderHistory = fetchOrderHistory(id);

        return new UserDetailsDTO(user, orderHistory);
    }

    @SneakyThrows
    private List<Map<String, Object>> fetchOrderHistory(Long userId) {
        String orderServiceUrl = "http://order-management:8081/api/orders/user/" + userId;
        ResponseEntity<String> orderResponse = oauth2ClientService.get(orderServiceUrl);
        if (orderResponse.getStatusCode().is2xxSuccessful()) {
            return objectMapper.readValue(orderResponse.getBody(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } else if (orderResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new OrderNotFoundException("Order history not found for user: " + userId);
        } else {
            log.error("Failed to fetch order history for user {}. Status: {}", userId, orderResponse.getStatusCode());
            throw new RuntimeException("Failed to fetch order history. Please try again later.");
        }
    }

    @Cacheable(value = "users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @CachePut(value = "users", key = "#user.id")
    public User createUser(User user) {
        User savedUser = userRepository.save(user);

        log.info("User created successfully. User ID: {}", savedUser.getId());
        kafkaTemplate.send("user-events", "User created: " + savedUser.getId());

        return savedUser;
    }

    @CachePut(value = "users", key = "#id")
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully. User ID: {}", updatedUser.getId());
        return updatedUser;
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        kafkaTemplate.send("user-events", "User deleted: " + id);
        log.info("User deleted successfully. User ID: {}", id);
    }
}
