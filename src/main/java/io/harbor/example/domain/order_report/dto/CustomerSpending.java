package io.harbor.example.domain.order_report.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class CustomerSpending {
    Long customerId;
    String email;
    Long orderCount;
    BigDecimal totalGross;
}
