package io.harbor.example.domain.order_report;

import io.harbor.example.H2IntegrationTest;
import io.harbor.example.domain.customer.CustomerFacade;
import io.harbor.example.domain.customer.dto.command.CustomerChangeCommand;
import io.harbor.example.domain.order.OrderFacade;
import io.harbor.example.domain.order.dto.OrderStatus;
import io.harbor.example.domain.order.dto.command.OrderCreateCommand;
import io.harbor.example.domain.order_report.dto.CustomerSpending;
import io.harbor.example.domain.order_report.dto.OrderReport;
import io.harbor.example.domain.product.ProductFacade;
import io.harbor.example.domain.product.dto.ProductId;
import io.harbor.example.domain.product.dto.command.ProductCreateCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class OrderReportFacadeTest extends H2IntegrationTest {

    private static final byte[] PHOTO = {1, 2, 3};
    private static final BigDecimal NET_PRICE = new BigDecimal("100.00");
    private static final BigDecimal GROSS_PRICE = new BigDecimal("123.00");

    @Autowired
    private OrderReportFacade orderReportFacade;

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private CustomerFacade customerFacade;

    @Autowired
    private ProductFacade productFacade;

    @Test
    void findAllMapsAllViewColumns() {
        Long customerId = createCustomer("report-mapping@example.com");
        createProduct("R-MAPPING", "C-1");
        Long orderId = createOrder(customerId, "R-MAPPING", "C-1");

        OrderReport report = findReport(orderId);

        assertThat(report.getCustomerId()).isEqualTo(customerId);
        assertThat(report.getCustomerEmail()).isEqualTo("report-mapping@example.com");
        assertThat(report.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(report.getCreationDate()).isCloseTo(OffsetDateTime.now(), within(1, ChronoUnit.MINUTES));
        assertThat(report.getCompleteDate()).isNull();
        assertThat(report.getProductId()).isNotNull();
        assertThat(report.getSeries()).isEqualTo("R-MAPPING");
        assertThat(report.getCode()).isEqualTo("C-1");
        assertThat(report.getTitle()).isEqualTo("Title");
        assertThat(report.getNetPrice()).isEqualByComparingTo(NET_PRICE);
        assertThat(report.getGrossPrice()).isEqualByComparingTo(GROSS_PRICE);
        assertThat(report.getSnapshotDateTime()).isNotNull();
    }

    @Test
    void findAllReflectsStatusChange() {
        Long customerId = createCustomer("report-status@example.com");
        createProduct("R-STATUS", "C-1");
        Long orderId = createOrder(customerId, "R-STATUS", "C-1");

        orderFacade.process(orderId);

        assertThat(findReport(orderId).getStatus()).isEqualTo(OrderStatus.PROCESSED);
    }

    @Test
    void findAllOrdersByCreationDateDesc() {
        Long customerId = createCustomer("report-ordering@example.com");
        createProduct("R-ORDERING", "C-1");
        Long firstOrderId = createOrder(customerId, "R-ORDERING", "C-1");
        Long secondOrderId = createOrder(customerId, "R-ORDERING", "C-1");

        List<OrderReport> reports = orderReportFacade.findAll();

        assertThat(reports).extracting(OrderReport::getId).contains(firstOrderId, secondOrderId);
        assertThat(reports).isSortedAccordingTo(
                Comparator.comparing(OrderReport::getCreationDate).reversed());
    }

    @Test
    void findSpendingsAboveAggregatesOrderCountAndTotal() {
        Long customerId = createCustomer("cte-aggregation@example.com");
        createProduct("R-CTE-AGG", "C-1");
        createOrder(customerId, "R-CTE-AGG", "C-1");
        createOrder(customerId, "R-CTE-AGG", "C-1");

        List<CustomerSpending> spendings = orderReportFacade.findSpendingsAbove(BigDecimal.ZERO);

        CustomerSpending spending = findSpending(spendings, customerId);
        assertThat(spending.getEmail()).isEqualTo("cte-aggregation@example.com");
        assertThat(spending.getOrderCount()).isEqualTo(2L);
        assertThat(spending.getTotalGross()).isEqualByComparingTo(GROSS_PRICE.multiply(BigDecimal.TWO));
    }

    @Test
    void findSpendingsAboveFiltersByThreshold() {
        Long lowSpender = createCustomer("cte-low@example.com");
        Long highSpender = createCustomer("cte-high@example.com");
        createProduct("R-CTE-FILTER", "C-1");
        createOrder(lowSpender, "R-CTE-FILTER", "C-1");
        createOrder(highSpender, "R-CTE-FILTER", "C-1");
        createOrder(highSpender, "R-CTE-FILTER", "C-1");

        List<CustomerSpending> spendings =
                orderReportFacade.findSpendingsAbove(GROSS_PRICE.multiply(BigDecimal.TWO));

        assertThat(spendings).extracting(CustomerSpending::getCustomerId)
                .contains(highSpender)
                .doesNotContain(lowSpender);
    }

    @Test
    void findSpendingsAboveOrdersByTotalGrossDesc() {
        Long smallSpender = createCustomer("cte-order-small@example.com");
        Long bigSpender = createCustomer("cte-order-big@example.com");
        createProduct("R-CTE-ORDER", "C-1");
        createOrder(smallSpender, "R-CTE-ORDER", "C-1");
        createOrder(bigSpender, "R-CTE-ORDER", "C-1");
        createOrder(bigSpender, "R-CTE-ORDER", "C-1");

        List<CustomerSpending> spendings = orderReportFacade.findSpendingsAbove(BigDecimal.ZERO);

        assertThat(spendings).extracting(CustomerSpending::getCustomerId)
                .contains(smallSpender, bigSpender);
        assertThat(spendings).isSortedAccordingTo(
                Comparator.comparing(CustomerSpending::getTotalGross).reversed());
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

    private OrderReport findReport(Long orderId) {
        return orderReportFacade.findAll().stream()
                .filter(report -> report.getId().equals(orderId))
                .findFirst()
                .orElseThrow();
    }

    private CustomerSpending findSpending(List<CustomerSpending> spendings, Long customerId) {
        return spendings.stream()
                .filter(spending -> spending.getCustomerId().equals(customerId))
                .findFirst()
                .orElseThrow();
    }
}
