package com.spring.soft.usermanagement.service;

import com.spring.soft.usermanagement.dto.UserDetails;
import com.spring.soft.usermanagement.entity.User;
import com.spring.soft.usermanagement.exception.UserNotFoundException;
import com.spring.soft.usermanagement.intgeration.OrderServiceClient;
import com.spring.soft.usermanagement.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderServiceClient orderServiceClient;
    private final Validator validator;

    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Cacheable(value = "users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
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
        return updateUserEntity(user, existingUser);
    }

    public UserDetails getUserDetailsById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .orderHistory(orderServiceClient.getOrdersByUserId(id))
                .build();
    }

    public UserDetails getUserDetailsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .orderHistory(orderServiceClient.getOrdersByUserId(user.getId()))
                .build();
    }

    @Transactional
    public void createOrUpdateUserFromKeycloak(String username, String email) {
        User user = userRepository.findByEmail(username)
                .orElse(new User());

        user.setUsername(username);
        user.setEmail(email);
        user.setId(0L);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<User> violation : violations) {
                sb.append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage())
                        .append("; ");
            }
            throw new DataIntegrityViolationException("Invalid user data: " + sb);
        }
        userRepository.save(user);
    }

    public User updateUserByEmail(String email, User user) {
        User existingUser = getUserByEmail(email);
        return updateUserEntity(user, existingUser);
    }

    private User updateUserEntity(User requestedUser, User existingUser) {
        existingUser.setUsername(requestedUser.getUsername());
        existingUser.setEmail(requestedUser.getEmail());
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
