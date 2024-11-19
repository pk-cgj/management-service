package com.spring.soft.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class Order {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("product")
    private String product;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private Instant createdAt;

    @JsonProperty("updatedAt")
    private Instant updatedAt;
}
