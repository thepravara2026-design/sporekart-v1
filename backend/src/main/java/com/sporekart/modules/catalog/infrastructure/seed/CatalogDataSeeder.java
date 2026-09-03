package com.sporekart.modules.catalog.infrastructure.seed;

import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.domain.product.ProductVariant;
import com.sporekart.modules.catalog.domain.product.QuantityUnit;
import com.sporekart.modules.catalog.infrastructure.persistence.CategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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

        // 1. Lion's Mane Extract (Liquid Extract with 100 ml, 250 ml, 500 ml, 1 L variants)
        createAndSaveProduct(
                "SKU-LION-001",
                "Lion's Mane Extract",
                "Organic Lion's Mane mushroom extract for cognitive focus and brain health.",
                new BigDecimal("999.00"),
                new BigDecimal("1299.00"),
                "INR",
                ProductStatus.ACTIVE,
                savedMedicinal,
                List.of(
                        new VariantSeed(new BigDecimal("100.00"), QuantityUnit.ML, new BigDecimal("999.00"), new BigDecimal("1299.00")),
                        new VariantSeed(new BigDecimal("250.00"), QuantityUnit.ML, new BigDecimal("1799.00"), new BigDecimal("2199.00")),
                        new VariantSeed(new BigDecimal("500.00"), QuantityUnit.ML, new BigDecimal("2499.00"), new BigDecimal("2999.00")),
                        new VariantSeed(new BigDecimal("1.00"), QuantityUnit.L, new BigDecimal("4499.00"), new BigDecimal("4999.00"))
                )
        );

        // 2. Reishi Mushroom Powder (Powder with 100 g, 250 g, 500 g, 1 kg variants)
        createAndSaveProduct(
                "SKU-REISHI-001",
                "Reishi Mushroom Powder",
                "Calming Reishi dual extract powder for stress relief and deep sleep.",
                new BigDecimal("799.00"),
                new BigDecimal("999.00"),
                "INR",
                ProductStatus.ACTIVE,
                savedMedicinal,
                List.of(
                        new VariantSeed(new BigDecimal("100.00"), QuantityUnit.G, new BigDecimal("799.00"), new BigDecimal("999.00")),
                        new VariantSeed(new BigDecimal("250.00"), QuantityUnit.G, new BigDecimal("1399.00"), new BigDecimal("1699.00")),
                        new VariantSeed(new BigDecimal("500.00"), QuantityUnit.G, new BigDecimal("1999.00"), new BigDecimal("2499.00")),
                        new VariantSeed(new BigDecimal("1.00"), QuantityUnit.KG, new BigDecimal("3599.00"), new BigDecimal("4199.00"))
                )
        );

        // 3. Cordyceps Energy Supplement (100 g, 250 g, 500 g)
        createAndSaveProduct(
                "SKU-CORDY-001",
                "Cordyceps Energy Supplement",
                "Natural stamina and energy boost from Cordyceps militaris.",
                new BigDecimal("1150.00"),
                null,
                "INR",
                ProductStatus.ACTIVE,
                savedMedicinal,
                List.of(
                        new VariantSeed(new BigDecimal("100.00"), QuantityUnit.G, new BigDecimal("1150.00"), null),
                        new VariantSeed(new BigDecimal("250.00"), QuantityUnit.G, new BigDecimal("1999.00"), null),
                        new VariantSeed(new BigDecimal("500.00"), QuantityUnit.G, new BigDecimal("2850.00"), null)
                )
        );

        // 4. Blue Oyster Growing Kit (500 g spawn, 1 kg kit, 2 kg commercial kit)
        createAndSaveProduct(
                "SKU-OYSTER-001",
                "Blue Oyster Growing Kit",
                "Easy to grow Blue Oyster mushrooms on your countertop.",
                new BigDecimal("799.00"),
                new BigDecimal("999.00"),
                "INR",
                ProductStatus.ACTIVE,
                savedCultivation,
                List.of(
                        new VariantSeed(new BigDecimal("500.00"), QuantityUnit.G, new BigDecimal("799.00"), new BigDecimal("999.00")),
                        new VariantSeed(new BigDecimal("1.00"), QuantityUnit.KG, new BigDecimal("1599.00"), new BigDecimal("1899.00")),
                        new VariantSeed(new BigDecimal("2.00"), QuantityUnit.KG, new BigDecimal("2799.00"), new BigDecimal("3299.00"))
                )
        );

        // 5. Organic Dried Shiitake (100 g, 250 g, 500 g, 1 kg)
        createAndSaveProduct(
                "SKU-SHIITAKE-001",
                "Organic Dried Shiitake",
                "Premium grade whole dried Shiitake mushrooms for gourmet cooking.",
                new BigDecimal("399.00"),
                new BigDecimal("499.00"),
                "INR",
                ProductStatus.ACTIVE,
                savedGourmet,
                List.of(
                        new VariantSeed(new BigDecimal("100.00"), QuantityUnit.G, new BigDecimal("399.00"), new BigDecimal("499.00")),
                        new VariantSeed(new BigDecimal("250.00"), QuantityUnit.G, new BigDecimal("699.00"), new BigDecimal("849.00")),
                        new VariantSeed(new BigDecimal("500.00"), QuantityUnit.G, new BigDecimal("1199.00"), null),
                        new VariantSeed(new BigDecimal("1.00"), QuantityUnit.KG, new BigDecimal("2199.00"), null)
                )
        );

        log.info("Finished seeding Catalog data with multi-unit variants.");
    }

    private void createAndSaveProduct(
            String sku,
            String name,
            String description,
            BigDecimal price,
            BigDecimal strikeOutPrice,
            String currency,
            ProductStatus status,
            Category category,
            List<VariantSeed> variantSeeds
    ) {
        Product p = Product.create(sku, name, description, price, strikeOutPrice, currency, category);
        if (variantSeeds != null) {
            for (VariantSeed vs : variantSeeds) {
                String variantSku = sku + "-" + vs.val().stripTrailingZeros().toPlainString() + vs.unit().getSymbol().toUpperCase();
                ProductVariant variant = ProductVariant.create(
                        p.getId(),
                        variantSku,
                        vs.val(),
                        vs.unit(),
                        vs.sellingPrice(),
                        vs.strikeOutPrice()
                );
                p.addVariant(variant);
            }
        }
        if (status != ProductStatus.DRAFT) {
            p.changeStatus(status);
        }
        productRepository.save(p);
    }

    private record VariantSeed(BigDecimal val, QuantityUnit unit, BigDecimal sellingPrice, BigDecimal strikeOutPrice) {}
}
