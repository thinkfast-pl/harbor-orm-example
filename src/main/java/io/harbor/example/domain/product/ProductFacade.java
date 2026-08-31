package io.harbor.example.domain.product;

import io.harbor.api.HarborSession;
import io.harbor.example.domain.product.dto.Product;
import io.harbor.example.domain.product.dto.ProductCategory;
import io.harbor.example.domain.product.dto.ProductId;
import io.harbor.example.domain.product.dto.command.ProductCategoryCreateCommand;
import io.harbor.example.domain.product.dto.command.ProductCategoryUpdateCommand;
import io.harbor.example.domain.product.dto.command.ProductCreateCommand;
import io.harbor.example.domain.product.dto.command.ProductUpdateCommand;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductFacade {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductFactory productFactory;
    private final ProductService productService;
    private final HarborSession session;

    public CommonCreateResult<UUID> createCategory(@NonNull ProductCategoryCreateCommand command) {
        final UUID id = UUID.randomUUID();
        productCategoryRepository.insert(ProductCategoryEntity.of(id, command));
        return new CommonCreateResult<>(id);
    }

    public Optional<ProductCategory> findCategoryById(@NonNull UUID id) {
        return productCategoryRepository.findById(id).map(ProductCategoryEntity::toDto);
    }

    public void updateCategory(@NonNull UUID id, @NonNull ProductCategoryUpdateCommand command) {
        ProductCategoryEntity entity = productCategoryRepository.findByIdOrThrow(id);
        entity.update(command);
        productCategoryRepository.update(entity);
    }

    public void deleteCategory(@NonNull UUID id) {
        ProductCategoryEntity entity = productCategoryRepository.findByIdOrThrow(id);
        productCategoryRepository.delete(entity);
    }

    public CommonCreateResult<ProductId> createProduct(@NonNull ProductCreateCommand command) {
        ProductEntity productEntity = productFactory.of(command);
        productRepository.insert(productEntity);
        return new CommonCreateResult<>(productEntity.getId().toDto());
    }

    public Optional<Product> findProductById(@NonNull String series, @NonNull String code) {
        return productRepository.findById(new ProductIdEmbedded(series, code)).map(p -> p.toDto(session));
    }

    public void updateProduct(@NonNull String series, @NonNull String code, @NonNull ProductUpdateCommand command) {
        ProductEntity entity = productRepository.findByIdForUpdateOrThrow(new ProductIdEmbedded(series, code));
        productService.update(entity, command);
        productRepository.update(entity);
    }

    public void deleteProductById(@NonNull String series, @NonNull String code) {
        productRepository.deleteById(new ProductIdEmbedded(series, code));
    }
}
