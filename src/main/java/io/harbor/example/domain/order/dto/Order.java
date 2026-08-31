package io.harbor.example.domain.order.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Value
public class Order {
    Long id;
    Long customerId;
    OrderStatus status;
    String series;
    String code;
    String title;
    BigDecimal netPrice;
    BigDecimal grossPrice;
    LocalDateTime snapshotDateTime;
    OffsetDateTime creationDate;
    OffsetDateTime completeDate;
}
