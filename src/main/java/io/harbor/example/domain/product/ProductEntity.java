package io.harbor.example.domain.product;

import io.harbor.api.HarborSession;
import io.harbor.api.annotations.*;
import io.harbor.api.lob.PortableBlob;
import io.harbor.api.lob.PortableClob;
import io.harbor.example.domain.product.dto.Product;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Entity(table = "products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class ProductEntity {

    @Id
    @Embedded
    private ProductIdEmbedded id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private PortableClob description;

    @Column(nullable = false)
    private PortableBlob photo;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private BigDecimal netPrice;

    @Column(nullable = false)
    private BigDecimal grossPrice;

    @ManyToMany(
            table = "products_categories",
            joinColumns = {
                    @JoinColumn(name = "product_series", fieldType = String.class, referencedColumnName = "series"),
                    @JoinColumn(name = "product_code", fieldType = String.class, referencedColumnName = "code"),
            },
            inverseJoinColumns = @JoinColumn(name = "product_category_id", fieldType = UUID.class)
    )
    private Set<ProductCategoryEntity> categories;

    @SneakyThrows
    Product toDto(HarborSession session) {
        return new Product(
                id.toDto(),
                title,
                session.readClobAllChars(description),
                session.readBlobAllBytes(photo),
                active,
                netPrice,
                grossPrice,
                categories.stream().map(ProductCategoryEntity::toDto).toList()
        );
    }
}
