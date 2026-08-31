package io.harbor.example.domain.order_report;

import io.harbor.api.annotations.Column;
import io.harbor.api.annotations.Enumerated;
import io.harbor.api.annotations.TypeHandler;
import io.harbor.api.annotations.View;
import io.harbor.api.dialect.StandardDialects;
import io.harbor.api.metadata.EnumMappingType;
import io.harbor.core.sql.OffsetDateTimeAsTimestampTypeHandler;
import io.harbor.example.domain.order.dto.OrderStatus;
import io.harbor.example.domain.order_report.dto.OrderReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@View(name = "orders_reports")
@NoArgsConstructor
@AllArgsConstructor
@Getter
class OrderReportView {

    @Column(name = "order_id", nullable = false)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumMappingType.ORDINAL)
    private OrderStatus status;

    @Column(name = "creation_date", nullable = false)
    @TypeHandler(dialect = StandardDialects.MARIADB, value = OffsetDateTimeAsTimestampTypeHandler.class)
    @TypeHandler(dialect = StandardDialects.MYSQL, value = OffsetDateTimeAsTimestampTypeHandler.class)
    private OffsetDateTime creationDate;

    @Column(name = "complete_date", nullable = true)
    @TypeHandler(dialect = StandardDialects.MARIADB, value = OffsetDateTimeAsTimestampTypeHandler.class)
    @TypeHandler(dialect = StandardDialects.MYSQL, value = OffsetDateTimeAsTimestampTypeHandler.class)
    private OffsetDateTime completeDate;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_series", nullable = false)
    private String series;

    @Column(name = "product_code", nullable = false)
    private String code;

    @Column(name = "product_title", nullable = false)
    private String title;

    @Column(name = "product_net_price", nullable = false)
    private BigDecimal netPrice;

    @Column(name = "product_gross_price", nullable = false)
    private BigDecimal grossPrice;

    @Column(name = "snapshot_date_time", nullable = false)
    private LocalDateTime snapshotDateTime;

    OrderReport toDto() {
        return new OrderReport(
                id,
                customerId,
                customerEmail,
                status,
                creationDate,
                completeDate,
                productId,
                series,
                code,
                title,
                netPrice,
                grossPrice,
                snapshotDateTime
        );
    }
}
