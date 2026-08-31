package io.harbor.example.domain.product.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
public class Product {
    ProductId id;
    String title;
    String description;
    byte[] photo;
    boolean active;
    BigDecimal netPrice;
    BigDecimal grossPrice;
    List<ProductCategory> categories;
}
