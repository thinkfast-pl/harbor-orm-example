package io.harbor.example.domain.order;

import io.harbor.api.LazyRef;
import io.harbor.example.domain.order.dto.OrderStatus;
import io.harbor.example.domain.order.dto.command.OrderCreateCommand;
import io.harbor.example.domain.product.ProductFacade;
import io.harbor.example.domain.product.dto.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
class OrderFactory {
    private final ProductFacade productFacade;

    OrderEntity of(OrderCreateCommand command) {
        Product product = productFacade.findProductById(command.getProductId().getSeries(), command.getProductId().getCode()).orElseThrow();
        return new OrderEntity(
                null,
                command.getCustomerId(),
                OrderStatus.NEW,
                LazyRef.of(OrderProductEntity.of(product)),
                OffsetDateTime.now(),
                null
        );
    }
}
