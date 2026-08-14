package com.sporekart.modules.cart.infrastructure.adapter;

import com.sporekart.modules.cart.domain.exception.ProductNotPurchasableException;
import com.sporekart.modules.cart.domain.port.CatalogPort;
import com.sporekart.modules.catalog.application.ProductApplicationService;
import com.sporekart.modules.catalog.application.ProductDto;
import com.sporekart.modules.catalog.domain.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CatalogAdapter implements CatalogPort {

    private final ProductApplicationService productApplicationService;

    public CatalogAdapter(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @Override
    public CatalogProductDetails getProductForCart(UUID productId) {
        try {
            ProductDto productDto = productApplicationService.getProductById(productId);
            String statusName = productDto.status() != null ? productDto.status().name() : "INACTIVE";
            boolean purchasable = "ACTIVE".equalsIgnoreCase(statusName);
            if (!purchasable) {
                throw new ProductNotPurchasableException(productId, "Product status is " + statusName);
            }
            return new CatalogProductDetails(
                    productDto.id(),
                    productDto.name(),
                    productDto.sku(),
                    productDto.price(),
                    productDto.currency(),
                    true,
                    statusName
            );
        } catch (ProductNotFoundException e) {
            throw e;
        }
    }
}
