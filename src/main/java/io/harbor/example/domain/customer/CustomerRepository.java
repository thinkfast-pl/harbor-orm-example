package io.harbor.example.domain.customer;

import io.harbor.api.HarborSession;
import io.harbor.core.repository.EntityRepository;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

@Repository
class CustomerRepository extends EntityRepository<CustomerEntity, Long> {
    private static final QCustomerEntity CUSTOMER = new QCustomerEntity("c");

    CustomerRepository(@NonNull HarborSession session) {
        super(session, CUSTOMER);
    }
}
