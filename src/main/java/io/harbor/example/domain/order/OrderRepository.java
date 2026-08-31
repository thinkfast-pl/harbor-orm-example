package io.harbor.example.domain.order;

import io.harbor.api.HarborSession;
import io.harbor.api.expression.DSL;
import io.harbor.api.expression.Expression;
import io.harbor.api.query.result.Record;
import io.harbor.core.repository.EntityRepository;
import io.harbor.example.db.CustomersTable;
import io.harbor.example.db.OrdersProductsTable;
import io.harbor.example.db.OrdersTable;
import io.harbor.example.domain.order.dto.CustomerWithOrders;
import io.harbor.example.domain.order.dto.Order;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
class OrderRepository extends EntityRepository<OrderEntity, Long> {
    private static final QOrderEntity ORDER = new QOrderEntity("o");

    OrderRepository(@NonNull HarborSession session) {
        super(session, ORDER);
    }

    List<CustomerWithOrders> findCustomerWithOrders() {
        CustomersTable c = new CustomersTable("c");
        OrdersTable o = new OrdersTable("o");
        OrdersProductsTable op = new OrdersProductsTable("op");

        Expression<Record[]> orders = DSL.multisetAgg(List.of(
                o.id,
                o.customerId,
                o.status,
                o.creationDate,
                o.completeDate,
                op.id,
                op.series,
                op.code,
                op.title,
                op.netPrice,
                op.grossPrice,
                op.snapshotDateTime
        ));

        return session
                .select(
                        c.id,
                        c.email,
                        orders
                )
                .from(c)
                .join(o).on(c.id.eq(o.customerId))
                .join(op).on(op.orderId.eq(o.id))
                .groupBy(c.id, c.email)
                .fetchAll()
                .stream()
                .map(record -> new CustomerWithOrders(
                        record.get(c.id),
                        record.get(c.email),
                        Stream.of(record.get(orders))
                                .map(or -> new Order(
                                        or.get(o.id),
                                        or.get(o.customerId),
                                        or.get(o.status),
                                        or.get(op.series),
                                        or.get(op.code),
                                        or.get(op.title),
                                        or.get(op.netPrice),
                                        or.get(op.grossPrice),
                                        or.get(op.snapshotDateTime),
                                        or.get(o.creationDate),
                                        or.get(o.completeDate)
                                ))
                                .toList()
                ))
                .toList();
    }
}
