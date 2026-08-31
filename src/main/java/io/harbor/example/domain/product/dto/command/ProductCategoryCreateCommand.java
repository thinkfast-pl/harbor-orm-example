package io.harbor.example.domain.product.dto.command;

import lombok.Value;

@Value
public class ProductCategoryCreateCommand {
    String name;
    boolean active;
}
