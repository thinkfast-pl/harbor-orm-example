package io.harbor.example.domain.order_report.dto;

import io.harbor.example.domain.order.dto.OrderStatus;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
public class OrderReport {
    Long id;
    Long customerId;
    String customerEmail;
    OrderStatus status;
    OffsetDateTime creationDate;
    OffsetDateTime completeDate;
    UUID productId;
    String series;
    String code;
    String title;
    BigDecimal netPrice;
    BigDecimal grossPrice;
    LocalDateTime snapshotDateTime;
}
