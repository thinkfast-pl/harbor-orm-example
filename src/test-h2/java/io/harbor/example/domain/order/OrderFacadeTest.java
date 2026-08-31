package io.harbor.example.domain.order;

import io.harbor.api.repository.EntityNotFoundException;
import io.harbor.example.H2IntegrationTest;
import io.harbor.example.domain.customer.CustomerFacade;
import io.harbor.example.domain.customer.dto.command.CustomerChangeCommand;
import io.harbor.example.domain.order.dto.CustomerWithOrders;
import io.harbor.example.domain.order.dto.Order;
import io.harbor.example.domain.order.dto.OrderStatus;
import io.harbor.example.domain.order.dto.command.OrderCreateCommand;
import io.harbor.example.domain.product.ProductFacade;
import io.harbor.example.domain.product.dto.ProductId;
import io.harbor.example.domain.product.dto.command.ProductCreateCommand;
import io.harbor.example.domain.product.dto.command.ProductUpdateCommand;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderFacadeTest extends H2IntegrationTest {

    private static final long UNKNOWN_ID = 999_999_999L;
    private static final String UNKNOWN_SERIES = "UNKNOWN-SERIES";
    private static final String UNKNOWN_CODE = "UNKNOWN-CODE";
    private static final byte[] PHOTO = {1, 2, 3};
    private static final BigDecimal NET_PRICE = new BigDecimal("100.00");
    private static final BigDecimal GROSS_PRICE = new BigDecimal("123.00");
    private static final BigDecimal UPDATED_NET_PRICE = new BigDecimal("50.00");
    private static final BigDecimal UPDATED_GROSS_PRICE = new BigDecimal("61.50");

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private CustomerFacade customerFacade;

    @Autowired
    private ProductFacade productFacade;

    @Test
    void createReturnsGeneratedId() {
        Long customerId = createCustomer("order-create@example.com");
        createProduct("O-CREATE", "C-1");

        CommonCreateResult<Long> result = orderFacade.create(
                new OrderCreateCommand(customerId, new ProductId("O-CREATE", "C-1")));

        assertThat(result.getId()).isNotNull();
    }

    @Test
    void createdOrderCanBeFoundById() {
        Long customerId = createCustomer("order-find@example.com");
        createProduct("O-FIND", "C-1");
        Long orderId = createOrder(customerId, "O-FIND", "C-1");

        Optional<Order> found = orderFacade.findById(orderId);

        assertThat(found).isPresent();
        Order order = found.get();
        assertThat(order.getId()).isEqualTo(orderId);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(order.getSeries()).isEqualTo("O-FIND");
        assertThat(order.getCode()).isEqualTo("C-1");
        assertThat(order.getTitle()).isEqualTo("Title");
        assertThat(order.getNetPrice()).isEqualByComparingTo(NET_PRICE);
        assertThat(order.getGrossPrice()).isEqualByComparingTo(GROSS_PRICE);
        assertThat(order.getSnapshotDateTime()).isNotNull();
        assertThat(order.getCreationDate()).isCloseTo(OffsetDateTime.now(), within(1, ChronoUnit.MINUTES));
        assertThat(order.getCompleteDate()).isNull();
    }

    @Test
    void createThrowsForUnknownProduct() {
        Long customerId = createCustomer("order-unknown-product@example.com");
        OrderCreateCommand command = new OrderCreateCommand(customerId, new ProductId(UNKNOWN_SERIES, UNKNOWN_CODE));

        assertThatThrownBy(() -> orderFacade.create(command))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertThat(orderFacade.findById(UNKNOWN_ID)).isEmpty();
    }

    @Test
    void processMarksOrderProcessed() {
        Long customerId = createCustomer("order-process@example.com");
        createProduct("O-PROCESS", "C-1");
        Long orderId = createOrder(customerId, "O-PROCESS", "C-1");

        orderFacade.process(orderId);

        Order order = orderFacade.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSED);
        assertThat(order.getCompleteDate()).isNull();
    }

    @Test
    void processThrowsForUnknownId() {
        assertThatThrownBy(() -> orderFacade.process(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void cancelMarksOrderCancelled() {
        Long customerId = createCustomer("order-cancel@example.com");
        createProduct("O-CANCEL", "C-1");
        Long orderId = createOrder(customerId, "O-CANCEL", "C-1");

        orderFacade.cancel(orderId);

        Order order = orderFacade.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCompleteDate()).isNull();
    }

    @Test
    void cancelThrowsForUnknownId() {
        assertThatThrownBy(() -> orderFacade.cancel(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void orderKeepsProductSnapshotAfterProductPriceChange() {
        Long customerId = createCustomer("order-snapshot@example.com");
        createProduct("O-SNAPSHOT", "C-1");
        Long orderId = createOrder(customerId, "O-SNAPSHOT", "C-1");

        productFacade.updateProduct("O-SNAPSHOT", "C-1", new ProductUpdateCommand(
                "New title", "Description", PHOTO, true, UPDATED_NET_PRICE, UPDATED_GROSS_PRICE, Set.of()));

        Order order = orderFacade.findById(orderId).orElseThrow();
        assertThat(order.getTitle()).isEqualTo("Title");
        assertThat(order.getNetPrice()).isEqualByComparingTo(NET_PRICE);
        assertThat(order.getGrossPrice()).isEqualByComparingTo(GROSS_PRICE);
    }

    @Test
    void findCustomerWithOrdersAggregatesOrdersPerCustomer() {
        Long customerId = createCustomer("cwo-aggregate@example.com");
        createProduct("CWO-AGG", "C-1");
        createProduct("CWO-AGG", "C-2");
        Long firstOrderId = createOrder(customerId, "CWO-AGG", "C-1");
        Long secondOrderId = createOrder(customerId, "CWO-AGG", "C-2");

        List<CustomerWithOrders> result = orderFacade.findCustomerWithOrders();

        assertThat(result).hasSize(1);
        CustomerWithOrders customer = result.getFirst();
        assertThat(customer.getId()).isEqualTo(customerId);
        assertThat(customer.getEmail()).isEqualTo("cwo-aggregate@example.com");
        assertThat(customer.getOrders())
                .extracting(Order::getId)
                .containsExactlyInAnyOrder(firstOrderId, secondOrderId);
    }

    @Test
    void findCustomerWithOrdersMapsAllOrderFields() {
        Long customerId = createCustomer("cwo-mapping@example.com");
        createProduct("CWO-MAP", "C-1");
        Long orderId = createOrder(customerId, "CWO-MAP", "C-1");

        List<CustomerWithOrders> result = orderFacade.findCustomerWithOrders();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getOrders()).hasSize(1);
        Order order = result.getFirst().getOrders().getFirst();
        assertThat(order.getId()).isEqualTo(orderId);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(order.getSeries()).isEqualTo("CWO-MAP");
        assertThat(order.getCode()).isEqualTo("C-1");
        assertThat(order.getTitle()).isEqualTo("Title");
        assertThat(order.getNetPrice()).isEqualByComparingTo(NET_PRICE);
        assertThat(order.getGrossPrice()).isEqualByComparingTo(GROSS_PRICE);
        assertThat(order.getSnapshotDateTime()).isNotNull();
        assertThat(order.getCreationDate()).isCloseTo(OffsetDateTime.now(), within(1, ChronoUnit.MINUTES));
        assertThat(order.getCompleteDate()).isNull();
    }

    @Test
    void findCustomerWithOrdersGroupsOrdersByCustomer() {
        Long firstCustomerId = createCustomer("cwo-first@example.com");
        Long secondCustomerId = createCustomer("cwo-second@example.com");
        createProduct("CWO-GROUP", "C-1");
        Long firstOrderId = createOrder(firstCustomerId, "CWO-GROUP", "C-1");
        Long secondOrderId = createOrder(secondCustomerId, "CWO-GROUP", "C-1");

        List<CustomerWithOrders> result = orderFacade.findCustomerWithOrders();

        assertThat(result).hasSize(2);
        assertThat(result)
                .filteredOn(c -> c.getId().equals(firstCustomerId))
                .singleElement()
                .satisfies(c -> assertThat(c.getOrders())
                        .extracting(Order::getId)
                        .containsExactly(firstOrderId));
        assertThat(result)
                .filteredOn(c -> c.getId().equals(secondCustomerId))
                .singleElement()
                .satisfies(c -> assertThat(c.getOrders())
                        .extracting(Order::getId)
                        .containsExactly(secondOrderId));
    }

    @Test
    void findCustomerWithOrdersExcludesCustomerWithoutOrders() {
        Long customerWithOrderId = createCustomer("cwo-with-order@example.com");
        createCustomer("cwo-without-order@example.com");
        createProduct("CWO-EXCL", "C-1");
        createOrder(customerWithOrderId, "CWO-EXCL", "C-1");

        List<CustomerWithOrders> result = orderFacade.findCustomerWithOrders();

        assertThat(result)
                .extracting(CustomerWithOrders::getId)
                .containsExactly(customerWithOrderId);
    }

    @Test
    void findCustomerWithOrdersReturnsEmptyListWhenNoOrders() {
        assertThat(orderFacade.findCustomerWithOrders()).isEmpty();
    }

    private Long createCustomer(String email) {
        return customerFacade.create(new CustomerChangeCommand(email, List.of())).getId();
    }

    private void createProduct(String series, String code) {
        productFacade.createProduct(new ProductCreateCommand(
                series, code, "Title", "Description", PHOTO, NET_PRICE, GROSS_PRICE, Set.of()));
    }

    private Long createOrder(Long customerId, String series, String code) {
        return orderFacade.create(new OrderCreateCommand(customerId, new ProductId(series, code))).getId();
    }
}
