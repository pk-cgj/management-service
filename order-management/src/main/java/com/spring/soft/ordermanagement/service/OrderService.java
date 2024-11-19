package com.spring.soft.ordermanagement.service;

import com.spring.soft.ordermanagement.dto.User;
import com.spring.soft.ordermanagement.entity.Order;
import com.spring.soft.ordermanagement.exception.OrderNotFoundException;
import com.spring.soft.ordermanagement.exception.UserNotFoundException;
import com.spring.soft.ordermanagement.intgeration.UserServiceClient;
import com.spring.soft.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final UserServiceClient userServiceClient;

    @Cacheable(value = "orders", key = "#id")
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
    }

    @Cacheable(value = "orders")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @CachePut(value = "orders", key = "#order.id")
    public Order createOrder(Order order) {
        User user = userServiceClient.getUserByUserId(order.getUserId());
        if (user == null) {
            log.error("User not found or inaccessible. User ID: {}", order.getUserId());
            throw new UserNotFoundException("User not found or inaccessible");
        }

        Order savedOrder = orderRepository.save(order);
        kafkaTemplate.send("order-events", "Order created: " + savedOrder.getId());
        log.info("Order created successfully. Order ID: {}", savedOrder.getId());

        return savedOrder;
    }

    @CachePut(value = "orders", key = "#id")
    public Order updateOrder(Long id, Order order) {
        Order existingOrder = getOrderById(id);

        existingOrder.setUserId(order.getUserId());
        existingOrder.setProduct(order.getProduct());
        existingOrder.setQuantity(order.getQuantity());
        existingOrder.setPrice(order.getPrice());
        existingOrder.setStatus(order.getStatus());

        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("Order updated successfully. Order ID: {}", updatedOrder.getId());
        return updatedOrder;
    }

    @CacheEvict(value = "orders", key = "#id")
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
        kafkaTemplate.send("order-events", "Order deleted: " + id);
        log.info("Order deleted successfully. Order ID: {}", id);
    }
}
