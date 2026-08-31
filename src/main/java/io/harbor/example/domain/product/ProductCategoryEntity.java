package io.harbor.example.domain.product;

import io.harbor.api.annotations.*;
import io.harbor.example.domain.product.dto.ProductCategory;
import io.harbor.example.domain.product.dto.command.ProductCategoryCreateCommand;
import io.harbor.example.domain.product.dto.command.ProductCategoryUpdateCommand;
import io.harbor.example.shared.model.converter.BooleanToYesNoConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(table = "product_categories")
@NoArgsConstructor
@AllArgsConstructor
@Getter
class ProductCategoryEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Convert(converter = BooleanToYesNoConverter.class)
    private boolean active;

    @Column(nullable = false)
    @Version
    private long version;

    static ProductCategoryEntity of(UUID id, ProductCategoryCreateCommand command) {
        return new ProductCategoryEntity(
                id,
                command.getName(),
                command.isActive(),
                1
        );
    }

    void update(ProductCategoryUpdateCommand command) {
        this.name = command.getName();
        this.active = command.isActive();
        this.version = command.getVersion();
    }

    ProductCategory toDto() {
        return new ProductCategory(
                id,
                name,
                active,
                version
        );
    }
}
