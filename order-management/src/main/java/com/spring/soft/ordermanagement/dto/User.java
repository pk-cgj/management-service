package com.spring.soft.ordermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class User {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;
}