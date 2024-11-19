package com.spring.soft.usermanagement.controller;

import com.spring.soft.usermanagement.dto.UserDetails;
import com.spring.soft.usermanagement.entity.User;
import com.spring.soft.usermanagement.exception.UserNotFoundException;
import com.spring.soft.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/users/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUser() {
        return executeUserOperation(userService::getUserByEmail);
    }

    @GetMapping("/users/me/details")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUserDetails() {
        return executeUserOperation(userService::getUserDetailsByEmail);
    }

    @PutMapping("/users/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateOwnProfile(@Valid @RequestBody User user) {
        return executeUserOperation(email -> userService.updateUserByEmail(email, user));
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasAuthority('order:read') or hasRole('ADMIN')")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/admin/users/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetails> getUserDetails(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDetailsById(id));
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.created(URI.create("/api/admin/users/" + createdUser.getId())).body(createdUser);
    }

    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> executeUserOperation(Function<String, Object> operation) {
        try {
            String email = getEmailFromJwt();
            if (email != null) {
                Object result = operation.apply(email);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(result);
            }
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "User not authenticated or email not available");
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception e) {
            log.error("Error executing user operation", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }

    private String getEmailFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaim("email");
        }
        return null;
    }

    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"" + message + "\"}");
    }
}
