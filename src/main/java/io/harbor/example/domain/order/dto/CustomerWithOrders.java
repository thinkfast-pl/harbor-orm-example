package io.harbor.example.domain.order.dto;

import lombok.Value;

import java.util.List;

@Value
public class CustomerWithOrders {
    Long id;
    String email;
    List<Order> orders;
}
