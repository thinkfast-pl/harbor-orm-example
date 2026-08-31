package io.harbor.example.domain.product;

import io.harbor.api.annotations.Column;
import io.harbor.api.annotations.Embeddable;
import io.harbor.example.domain.product.dto.ProductId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ProductIdEmbedded {

    @Column(nullable = false)
    private String series;

    @Column(nullable = false)
    private String code;

    ProductId toDto() {
        return new ProductId(series, code);
    }
}


