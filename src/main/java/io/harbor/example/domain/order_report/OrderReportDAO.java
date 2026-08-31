package io.harbor.example.domain.order_report;

import io.harbor.api.HarborSession;
import io.harbor.api.expression.CommonTableExpression;
import io.harbor.api.expression.DSL;
import io.harbor.example.db.CustomersTable;
import io.harbor.example.db.OrdersProductsTable;
import io.harbor.example.db.OrdersTable;
import io.harbor.example.domain.order_report.dto.CustomerSpending;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
class OrderReportDAO {
    private static final QOrderReportView ORDER_REPORT = new QOrderReportView(null);

    private final HarborSession session;

    List<OrderReportView> findAll() {
        return session.select(ORDER_REPORT)
                .orderBy(ORDER_REPORT.creationDate.desc())
                .fetchAll();
    }

    List<CustomerSpending> findSpendingsAbove(BigDecimal minTotalGross) {
        OrdersTable o = new OrdersTable("o");
        OrdersProductsTable op = new OrdersProductsTable("op");
        CustomersTable c = new CustomersTable("c");

        CommonTableExpression spending = new CommonTableExpression("customer_spending")
                .as(DSL.select(o.customerId, DSL.countDistinct(o.id), DSL.sum(op.grossPrice))
                        .from(o)
                        .join(op).on(op.orderId.eq(o.id))
                        .groupBy(o.customerId));
        CommonTableExpression.Column<Long> customerId = spending.column("customer_id", o.customerId);
        CommonTableExpression.Column<Long> orderCount = spending.column("order_count", Long.class);
        CommonTableExpression.Column<BigDecimal> totalGross = spending.column("total_gross", BigDecimal.class);

        return session.with(spending)
                .select(c.id, c.email, orderCount, totalGross)
                .from(spending)
                .innerJoin(c).on(c.id.eq(customerId))
                .where(totalGross.ge(minTotalGross))
                .orderBy(totalGross.desc())
                .fetchAll().stream()
                .map(record -> new CustomerSpending(
                        record.get(c.id),
                        record.get(c.email),
                        record.get(orderCount),
                        record.get(totalGross)))
                .toList();
    }
}
