package com.spring.soft.usermanagement.dto;

import com.spring.soft.usermanagement.entity.User;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserDetailsDTO {
    private Long id;
    private String username;
    private String email;
    private List<Map<String, Object>> orderHistory;

    public UserDetailsDTO(User user, List<Map<String, Object>> orderHistory) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.orderHistory = orderHistory;
    }
}
