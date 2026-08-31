package io.harbor.example.domain.product;

import io.harbor.api.HarborSession;
import io.harbor.example.domain.product.dto.command.ProductCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
class ProductFactory {
    private final HarborSession session;
    private final ProductCategoryRepository productCategoryRepository;

    ProductEntity of(ProductCreateCommand command) {
        return new ProductEntity(
                new ProductIdEmbedded(command.getSeries(), command.getCode()),
                command.getTitle(),
                session.createClob(new StringReader(command.getDescription()), command.getDescription().length()),
                session.createBlob(new ByteArrayInputStream(command.getPhoto()), command.getPhoto().length),
                true,
                command.getNetPrice(),
                command.getGrossPrice(),
                new HashSet<>(productCategoryRepository.findAllById(command.getCategories()))
        );
    }
}
