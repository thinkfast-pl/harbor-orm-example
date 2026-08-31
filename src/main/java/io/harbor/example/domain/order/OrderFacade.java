package io.harbor.example.domain.order;

import io.harbor.example.domain.order.dto.CustomerWithOrders;
import io.harbor.example.domain.order.dto.Order;
import io.harbor.example.domain.order.dto.OrderStatus;
import io.harbor.example.domain.order.dto.command.OrderCreateCommand;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderRepository orderRepository;
    private final OrderFactory orderFactory;

    public CommonCreateResult<Long> create(@NonNull OrderCreateCommand command) {
        OrderEntity orderEntity = orderFactory.of(command);
        orderRepository.insert(orderEntity);
        return new CommonCreateResult<>(orderEntity.getId());
    }

    public Optional<Order> findById(@NonNull Long id) {
        return orderRepository.findById(id).map(OrderEntity::toDto);
    }

    public void process(@NonNull Long orderId) {
        OrderEntity orderEntity = orderRepository.findByIdForUpdateOrThrow(orderId);
        orderEntity.setStatus(OrderStatus.PROCESSED);
        orderRepository.update(orderEntity);
    }

    public void cancel(@NonNull Long orderId) {
        OrderEntity orderEntity = orderRepository.findByIdForUpdateOrThrow(orderId);
        orderEntity.setStatus(OrderStatus.CANCELLED);
        orderRepository.update(orderEntity);
    }

    public List<CustomerWithOrders> findCustomerWithOrders() {
        return orderRepository.findCustomerWithOrders();
    }
}
