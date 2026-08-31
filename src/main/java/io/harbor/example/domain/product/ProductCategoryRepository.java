package io.harbor.example.domain.product;

import io.harbor.api.HarborSession;
import io.harbor.core.repository.EntityRepository;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class ProductCategoryRepository extends EntityRepository<ProductCategoryEntity, UUID> {
    private static final QProductCategoryEntity PRODUCT_CATEGORY = new QProductCategoryEntity("pc");

    ProductCategoryRepository(@NonNull HarborSession session) {
        super(session, PRODUCT_CATEGORY);
    }
}
