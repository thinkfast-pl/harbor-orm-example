package io.harbor.example.domain.product;

import io.harbor.api.exception.OptimisticLockException;
import io.harbor.api.repository.EntityNotFoundException;
import io.harbor.example.MySqlIntegrationTest;
import io.harbor.example.domain.product.dto.Product;
import io.harbor.example.domain.product.dto.ProductCategory;
import io.harbor.example.domain.product.dto.ProductId;
import io.harbor.example.domain.product.dto.command.ProductCategoryCreateCommand;
import io.harbor.example.domain.product.dto.command.ProductCategoryUpdateCommand;
import io.harbor.example.domain.product.dto.command.ProductCreateCommand;
import io.harbor.example.domain.product.dto.command.ProductUpdateCommand;
import io.harbor.example.shared.dto.result.CommonCreateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductFacadeTest extends MySqlIntegrationTest {

    private static final String UNKNOWN_SERIES = "UNKNOWN-SERIES";
    private static final String UNKNOWN_CODE = "UNKNOWN-CODE";
    private static final byte[] PHOTO = {1, 2, 3, 4};
    private static final byte[] UPDATED_PHOTO = {9, 8, 7};
    private static final BigDecimal NET_PRICE = new BigDecimal("100.00");
    private static final BigDecimal GROSS_PRICE = new BigDecimal("123.00");
    private static final BigDecimal UPDATED_NET_PRICE = new BigDecimal("80.50");
    private static final BigDecimal UPDATED_GROSS_PRICE = new BigDecimal("99.02");

    @Autowired
    private ProductFacade productFacade;

    @Test
    void createCategoryReturnsGeneratedId() {
        CommonCreateResult<UUID> result = productFacade.createCategory(
                new ProductCategoryCreateCommand("cat-create", true));

        assertThat(result.getId()).isNotNull();
    }

    @Test
    void createdCategoryCanBeFoundById() {
        UUID id = createCategory("cat-find");

        Optional<ProductCategory> found = productFacade.findCategoryById(id);

        assertThat(found).isPresent();
        ProductCategory category = found.get();
        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getName()).isEqualTo("cat-find");
        assertThat(category.isActive()).isTrue();
        assertThat(category.getVersion()).isEqualTo(1L);
    }

    @Test
    void findCategoryByIdReturnsEmptyForUnknownId() {
        assertThat(productFacade.findCategoryById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void updateCategoryChangesNameActiveAndBumpsVersion() {
        UUID id = createCategory("cat-update");

        productFacade.updateCategory(id, new ProductCategoryUpdateCommand("cat-update-renamed", false, 1));

        ProductCategory category = productFacade.findCategoryById(id).orElseThrow();
        assertThat(category.getName()).isEqualTo("cat-update-renamed");
        assertThat(category.isActive()).isFalse();
        assertThat(category.getVersion()).isEqualTo(2L);
    }

    @Test
    void updateCategoryThrowsForUnknownId() {
        ProductCategoryUpdateCommand command = new ProductCategoryUpdateCommand("ghost", true, 1);

        assertThatThrownBy(() -> productFacade.updateCategory(UUID.randomUUID(), command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateCategoryWithStaleVersionThrowsOptimisticLockException() {
        UUID id = createCategory("cat-stale");
        ProductCategoryUpdateCommand staleCommand = new ProductCategoryUpdateCommand("cat-stale-renamed", true, 999);

        assertThatThrownBy(() -> productFacade.updateCategory(id, staleCommand))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void deleteCategoryRemovesCategory() {
        UUID id = createCategory("cat-delete");

        productFacade.deleteCategory(id);

        assertThat(productFacade.findCategoryById(id)).isEmpty();
    }

    @Test
    void deleteCategoryThrowsForUnknownId() {
        assertThatThrownBy(() -> productFacade.deleteCategory(UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createProductReturnsCompositeId() {
        CommonCreateResult<ProductId> result = productFacade.createProduct(new ProductCreateCommand(
                "S-CREATE", "C-1", "Title", "Description", PHOTO, NET_PRICE, GROSS_PRICE, Set.of()));

        assertThat(result.getId()).isEqualTo(new ProductId("S-CREATE", "C-1"));
    }

    @Test
    void createdProductCanBeFoundById() {
        UUID categoryId = createCategory("cat-product-find");
        productFacade.createProduct(new ProductCreateCommand(
                "S-FIND", "C-1", "Find title", "Find description", PHOTO, NET_PRICE, GROSS_PRICE, Set.of(categoryId)));

        Optional<Product> found = productFacade.findProductById("S-FIND", "C-1");

        assertThat(found).isPresent();
        Product product = found.get();
        assertThat(product.getId()).isEqualTo(new ProductId("S-FIND", "C-1"));
        assertThat(product.getTitle()).isEqualTo("Find title");
        assertThat(product.getDescription()).isEqualTo("Find description");
        assertThat(product.getPhoto()).isEqualTo(PHOTO);
        assertThat(product.isActive()).isTrue();
        assertThat(product.getNetPrice()).isEqualByComparingTo(NET_PRICE);
        assertThat(product.getGrossPrice()).isEqualByComparingTo(GROSS_PRICE);
        assertThat(product.getCategories())
                .extracting(ProductCategory::getId)
                .containsExactly(categoryId);
    }

    @Test
    void findProductByIdReturnsEmptyForUnknownId() {
        assertThat(productFacade.findProductById(UNKNOWN_SERIES, UNKNOWN_CODE)).isEmpty();
    }

    @Test
    void updateProductChangesFieldsAndReplacesCategories() {
        UUID initialCategoryId = createCategory("cat-product-update-initial");
        UUID replacementCategoryId = createCategory("cat-product-update-replacement");
        productFacade.createProduct(new ProductCreateCommand(
                "S-UPDATE", "C-1", "Old title", "Old description", PHOTO, NET_PRICE, GROSS_PRICE, Set.of(initialCategoryId)));

        productFacade.updateProduct("S-UPDATE", "C-1", new ProductUpdateCommand(
                "New title", "New description", UPDATED_PHOTO, false, UPDATED_NET_PRICE, UPDATED_GROSS_PRICE, Set.of(replacementCategoryId)));

        Product product = productFacade.findProductById("S-UPDATE", "C-1").orElseThrow();
        assertThat(product.getTitle()).isEqualTo("New title");
        assertThat(product.getDescription()).isEqualTo("New description");
        assertThat(product.getPhoto()).isEqualTo(UPDATED_PHOTO);
        assertThat(product.isActive()).isFalse();
        assertThat(product.getNetPrice()).isEqualByComparingTo(UPDATED_NET_PRICE);
        assertThat(product.getGrossPrice()).isEqualByComparingTo(UPDATED_GROSS_PRICE);
        assertThat(product.getCategories())
                .extracting(ProductCategory::getId)
                .containsExactly(replacementCategoryId);
    }

    @Test
    void updateProductChangesPricesOnly() {
        UUID categoryId = createCategory("cat-product-price-update");
        productFacade.createProduct(new ProductCreateCommand(
                "S-PRICE", "C-1", "Title", "Description", PHOTO, NET_PRICE, GROSS_PRICE, Set.of(categoryId)));

        productFacade.updateProduct("S-PRICE", "C-1", new ProductUpdateCommand(
                "Title", "Description", PHOTO, true, UPDATED_NET_PRICE, UPDATED_GROSS_PRICE, Set.of(categoryId)));

        Product product = productFacade.findProductById("S-PRICE", "C-1").orElseThrow();
        assertThat(product.getNetPrice()).isEqualByComparingTo(UPDATED_NET_PRICE);
        assertThat(product.getGrossPrice()).isEqualByComparingTo(UPDATED_GROSS_PRICE);
        assertThat(product.getTitle()).isEqualTo("Title");
        assertThat(product.isActive()).isTrue();
        assertThat(product.getCategories())
                .extracting(ProductCategory::getId)
                .containsExactly(categoryId);
    }

    @Test
    void updateProductThrowsForUnknownId() {
        ProductUpdateCommand command = new ProductUpdateCommand(
                "Title", "Description", PHOTO, true, NET_PRICE, GROSS_PRICE, Set.of());

        assertThatThrownBy(() -> productFacade.updateProduct(UNKNOWN_SERIES, UNKNOWN_CODE, command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void productCreatedWithoutCategoriesCanHaveCategoriesAdded() {
        productFacade.createProduct(new ProductCreateCommand(
                "S-ADD-CAT", "C-1", "Title", "Description", PHOTO, NET_PRICE, GROSS_PRICE, Set.of()));

        assertThat(productFacade.findProductById("S-ADD-CAT", "C-1").orElseThrow().getCategories()).isEmpty();

        UUID categoryId = createCategory("cat-product-added");
        productFacade.updateProduct("S-ADD-CAT", "C-1", new ProductUpdateCommand(
                "Title", "Description", PHOTO, true, NET_PRICE, GROSS_PRICE, Set.of(categoryId)));

        assertThat(productFacade.findProductById("S-ADD-CAT", "C-1").orElseThrow().getCategories())
                .extracting(ProductCategory::getId)
                .containsExactly(categoryId);
    }

    @Test
    void productCreatedWithCategoriesCanHaveAllCategoriesRemoved() {
        UUID categoryId = createCategory("cat-product-removed");
        productFacade.createProduct(new ProductCreateCommand(
                "S-REMOVE-CAT", "C-1", "Title", "Description", PHOTO, NET_PRICE, GROSS_PRICE, Set.of(categoryId)));

        productFacade.updateProduct("S-REMOVE-CAT", "C-1", new ProductUpdateCommand(
                "Title", "Description", PHOTO, true, NET_PRICE, GROSS_PRICE, Set.of()));

        assertThat(productFacade.findProductById("S-REMOVE-CAT", "C-1").orElseThrow().getCategories()).isEmpty();
    }

    @Test
    void deleteProductRemovesProductAndItsCategoryLinks() {
        UUID categoryId = createCategory("cat-product-delete");
        productFacade.createProduct(new ProductCreateCommand(
                "S-DELETE", "C-1", "Title", "Description", PHOTO, NET_PRICE, GROSS_PRICE, Set.of(categoryId)));

        productFacade.deleteProductById("S-DELETE", "C-1");

        assertThat(productFacade.findProductById("S-DELETE", "C-1")).isEmpty();
        assertThat(productFacade.findCategoryById(categoryId)).isPresent();
    }

    @Test
    void deleteProductIsNoOpForUnknownId() {
        productFacade.createProduct(new ProductCreateCommand(
                "S-SURVIVOR", "C-1", "Title", "Description", PHOTO, NET_PRICE, GROSS_PRICE, Set.of()));

        assertThatCode(() -> productFacade.deleteProductById(UNKNOWN_SERIES, UNKNOWN_CODE))
                .doesNotThrowAnyException();

        assertThat(productFacade.findProductById("S-SURVIVOR", "C-1")).isPresent();
    }

    private UUID createCategory(String name) {
        return productFacade.createCategory(new ProductCategoryCreateCommand(name, true)).getId();
    }
}
