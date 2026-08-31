package io.harbor.example.domain.product.dto;

import lombok.Value;

import java.util.UUID;

@Value
public class ProductCategory {
    UUID id;
    String name;
    boolean active;
    long version;
}
