package com.sporekart.modules.catalog.infrastructure.seed;

import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.CategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Profile({"dev", "qat"})
public class CatalogDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogDataSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CatalogDataSeeder(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!categoryRepository.findAll().isEmpty()) {
            log.info("Catalog data already exists. Skipping catalog data seeding.");
            return;
        }

        log.info("Seeding deterministic Catalog data for DEV/QAT profiles...");

        Category medicinal = Category.create("Medicinal Mushrooms", "Health and wellness mushroom extracts and powders");
        Category gourmet = Category.create("Gourmet Mushrooms", "Fresh and dried culinary mushrooms");
        Category cultivation = Category.create("Cultivation Kits", "All-in-one home mushroom growing kits");

        Category savedMedicinal = categoryRepository.save(medicinal);
        Category savedGourmet = categoryRepository.save(gourmet);
        Category savedCultivation = categoryRepository.save(cultivation);

        createAndSaveProduct("SKU-LION-001", "Lion's Mane Extract", "Organic Lion's Mane mushroom extract for cognitive focus.", new BigDecimal("2499.00"), new BigDecimal("2999.00"), "INR", ProductStatus.ACTIVE, savedMedicinal);
        createAndSaveProduct("SKU-REISHI-001", "Reishi Mushroom Powder", "Calming Reishi dual extract powder for stress relief.", new BigDecimal("1999.00"), new BigDecimal("2499.00"), "INR", ProductStatus.ACTIVE, savedMedicinal);
        createAndSaveProduct("SKU-CORDY-001", "Cordyceps Energy Supplement", "Natural stamina and energy boost from Cordyceps militaris.", new BigDecimal("2850.00"), null, "INR", ProductStatus.ACTIVE, savedMedicinal);
        createAndSaveProduct("SKU-OYSTER-001", "Blue Oyster Growing Kit", "Easy to grow Blue Oyster mushrooms on your countertop.", new BigDecimal("1599.00"), new BigDecimal("1899.00"), "INR", ProductStatus.ACTIVE, savedCultivation);
        createAndSaveProduct("SKU-SHIITAKE-001", "Organic Dried Shiitake", "Premium grade whole dried Shiitake mushrooms for cooking.", new BigDecimal("1199.00"), null, "INR", ProductStatus.ACTIVE, savedGourmet);

        log.info("Finished seeding Catalog data.");
    }

    private void createAndSaveProduct(String sku, String name, String description, BigDecimal price, BigDecimal strikeOutPrice, String currency, ProductStatus status, Category category) {
        Product p = Product.create(sku, name, description, price, strikeOutPrice, currency, category);
        if (status != ProductStatus.DRAFT) {
            p.changeStatus(status);
        }
        productRepository.save(p);
    }
}
