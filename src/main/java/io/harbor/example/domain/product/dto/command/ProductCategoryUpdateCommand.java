package io.harbor.example.domain.product.dto.command;

import lombok.Value;

@Value
public class ProductCategoryUpdateCommand {
    String name;
    boolean active;
    long version;
}
