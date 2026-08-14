package com.sporekart.modules.order;

import com.sporekart.modules.order.infrastructure.SequenceOrderNumberGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderNumberGeneratorTest {

    @Test
    @DisplayName("Should generate formatted order number from DB sequence")
    void testGenerateOrderNumberSuccess() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(100123L);

        SequenceOrderNumberGenerator generator = new SequenceOrderNumberGenerator(jdbcTemplate);
        String orderNumber = generator.generateOrderNumber();

        assertNotNull(orderNumber);
        assertTrue(orderNumber.startsWith("SPK-"));
        assertTrue(orderNumber.endsWith("-100123"));
    }

    @Test
    @DisplayName("Should fallback gracefully to atomic counter if DB sequence fails")
    void testGenerateOrderNumberFallback() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenThrow(new RuntimeException("DB offline"));

        SequenceOrderNumberGenerator generator = new SequenceOrderNumberGenerator(jdbcTemplate);
        String orderNumber1 = generator.generateOrderNumber();
        String orderNumber2 = generator.generateOrderNumber();

        assertNotNull(orderNumber1);
        assertNotNull(orderNumber2);
        assertTrue(orderNumber1.startsWith("SPK-"));
        assertTrue(orderNumber2.startsWith("SPK-"));
    }
}
