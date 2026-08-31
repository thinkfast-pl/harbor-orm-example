package io.harbor.example.domain.product;

import io.harbor.api.HarborSession;
import io.harbor.core.repository.EntityRepository;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

@Repository
class ProductRepository extends EntityRepository<ProductEntity, ProductIdEmbedded> {
    private static final QProductEntity PRODUCT = new QProductEntity("p");

    ProductRepository(@NonNull HarborSession session) {
        super(session, PRODUCT);
    }
}
