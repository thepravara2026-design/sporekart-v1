package com.sporekart.modules.catalog;

import com.sporekart.modules.catalog.application.dto.request.ProductSearchCriteria;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductSearchCriteriaTest {

    @Test
    void shouldNormalizeSearchStringByTrimmingWhitespace() {
        ProductSearchCriteria criteria = ProductSearchCriteria.of(
                0, 10, "name,asc", null, ProductStatus.ACTIVE, "  oyster mushroom  ", new BigDecimal("10.00"), new BigDecimal("50.00")
        );

        assertEquals("oyster mushroom", criteria.search());
        assertEquals(0, criteria.page());
        assertEquals(10, criteria.size());
        assertEquals("name,asc", criteria.sort());
        assertEquals(ProductStatus.ACTIVE, criteria.status());
        assertEquals(new BigDecimal("10.00"), criteria.minPrice());
        assertEquals(new BigDecimal("50.00"), criteria.maxPrice());
    }

    @Test
    void shouldConvertBlankSearchStringToNull() {
        ProductSearchCriteria criteria = ProductSearchCriteria.of(
                0, 20, null, null, null, "   ", null, null
        );

        assertNull(criteria.search());
    }

    @Test
    void shouldHandleNullFieldsGracefully() {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                null, null, null, null, null, 0, 20, null
        );

        assertNull(criteria.search());
        assertNull(criteria.categoryId());
        assertNull(criteria.status());
        assertNull(criteria.minPrice());
        assertNull(criteria.maxPrice());
    }
}
