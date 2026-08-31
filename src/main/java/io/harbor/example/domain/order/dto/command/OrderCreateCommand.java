package io.harbor.example.domain.order.dto.command;

import io.harbor.example.domain.product.dto.ProductId;
import lombok.Value;

@Value
public class OrderCreateCommand {
    Long customerId;
    ProductId productId;
}
