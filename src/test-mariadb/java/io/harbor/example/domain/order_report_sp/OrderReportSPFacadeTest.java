package io.harbor.example.domain.order_report_sp;

import io.harbor.example.MariaDbIntegrationTest;
import io.harbor.example.domain.customer.CustomerFacade;
import io.harbor.example.domain.customer.dto.command.CustomerChangeCommand;
import io.harbor.example.domain.order.OrderFacade;
import io.harbor.example.domain.order.dto.OrderStatus;
import io.harbor.example.domain.order.dto.command.OrderCreateCommand;
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

class OrderReportSPFacadeTest extends MariaDbIntegrationTest {

    private static final byte[] PHOTO = {1, 2, 3};
    private static final BigDecimal NET_PRICE = new BigDecimal("100.00");
    private static final BigDecimal GROSS_PRICE = new BigDecimal("123.00");

    @Autowired
    private OrderReportSPFacade orderReportSPFacade;

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private CustomerFacade customerFacade;

    @Autowired
    private ProductFacade productFacade;

    @Test
    void findOrderReportsMapsAllColumns() {
        Long customerId = createCustomer("sp-mapping@example.com");
        createProduct("SP-MAPPING", "C-1");
        Long orderId = createOrder(customerId, "SP-MAPPING", "C-1");

        List<OrderReport> reports = orderReportSPFacade.findOrderReports("SP-MAPPING", "C-1");

        assertThat(reports).hasSize(1);
        OrderReport report = reports.getFirst();
        assertThat(report.getId()).isEqualTo(orderId);
        assertThat(report.getCustomerId()).isEqualTo(customerId);
        assertThat(report.getCustomerEmail()).isEqualTo("sp-mapping@example.com");
        assertThat(report.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(report.getCreationDate()).isCloseTo(OffsetDateTime.now(), within(1, ChronoUnit.MINUTES));
        assertThat(report.getCompleteDate()).isNull();
        assertThat(report.getProductId()).isNotNull();
        assertThat(report.getSeries()).isEqualTo("SP-MAPPING");
        assertThat(report.getCode()).isEqualTo("C-1");
        assertThat(report.getTitle()).isEqualTo("Title");
        assertThat(report.getNetPrice()).isEqualByComparingTo(NET_PRICE);
        assertThat(report.getGrossPrice()).isEqualByComparingTo(GROSS_PRICE);
        assertThat(report.getSnapshotDateTime()).isNotNull();
    }

    @Test
    void findOrderReportsFiltersBySeriesAndCode() {
        Long customerId = createCustomer("sp-filtering@example.com");
        createProduct("SP-FILTERING", "C-1");
        createProduct("SP-FILTERING", "C-2");
        Long matchingOrderId = createOrder(customerId, "SP-FILTERING", "C-1");
        createOrder(customerId, "SP-FILTERING", "C-2");

        List<OrderReport> reports = orderReportSPFacade.findOrderReports("SP-FILTERING", "C-1");

        assertThat(reports).extracting(OrderReport::getId).containsExactly(matchingOrderId);
    }

    @Test
    void findOrderReportsReturnsEmptyForUnknownProduct() {
        assertThat(orderReportSPFacade.findOrderReports("SP-UNKNOWN", "C-1")).isEmpty();
    }

    @Test
    void findOrderReportsReturnsAllOrdersForProductNewestFirst() {
        Long customerId = createCustomer("sp-ordering@example.com");
        createProduct("SP-ORDERING", "C-1");
        Long firstOrderId = createOrder(customerId, "SP-ORDERING", "C-1");
        Long secondOrderId = createOrder(customerId, "SP-ORDERING", "C-1");

        List<OrderReport> reports = orderReportSPFacade.findOrderReports("SP-ORDERING", "C-1");

        assertThat(reports).extracting(OrderReport::getId)
                .containsExactlyInAnyOrder(firstOrderId, secondOrderId);
        assertThat(reports).isSortedAccordingTo(
                Comparator.comparing(OrderReport::getCreationDate).reversed());
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
