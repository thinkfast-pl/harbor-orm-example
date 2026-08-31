package io.harbor.example.domain.product.dto.command;

import lombok.Value;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Value
public class ProductUpdateCommand {
    String title;
    String description;
    byte[] photo;
    boolean active;
    BigDecimal netPrice;
    BigDecimal grossPrice;
    Set<UUID> categories;
}
