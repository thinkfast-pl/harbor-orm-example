package io.harbor.example.domain.product;

import io.harbor.api.HarborSession;
import io.harbor.example.domain.product.dto.command.ProductUpdateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

@Service
@RequiredArgsConstructor
class ProductService {
    private final ProductCategoryRepository productCategoryRepository;
    private final HarborSession session;

    void update(ProductEntity entity, ProductUpdateCommand command) {
        entity.setTitle(command.getTitle());
        session.updateClob(entity.getDescription(), new StringReader(command.getDescription()), command.getDescription().length());
        session.updateBlob(entity.getPhoto(), new ByteArrayInputStream(command.getPhoto()), command.getPhoto().length);
        entity.setActive(command.isActive());
        entity.setNetPrice(command.getNetPrice());
        entity.setGrossPrice(command.getGrossPrice());
        entity.getCategories().clear();
        entity.getCategories().addAll(productCategoryRepository.findAllById(command.getCategories()));
    }
}
