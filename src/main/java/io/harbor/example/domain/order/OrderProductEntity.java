package io.harbor.example.domain.order;

import io.harbor.api.annotations.Column;
import io.harbor.api.annotations.Entity;
import io.harbor.api.annotations.Id;
import io.harbor.example.domain.product.dto.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(table = "orders_products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class OrderProductEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String series;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private BigDecimal netPrice;

    @Column(nullable = false)
    private BigDecimal grossPrice;

    @Column(nullable = false)
    private LocalDateTime snapshotDateTime;

    static OrderProductEntity of(Product product) {
        return new OrderProductEntity(
                UUID.randomUUID(),
                product.getId().getSeries(),
                product.getId().getCode(),
                product.getTitle(),
                product.getNetPrice(),
                product.getGrossPrice(),
                LocalDateTime.now()
        );
    }
}
