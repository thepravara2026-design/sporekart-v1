package com.sporekart.modules.checkout.domain;

import com.sporekart.modules.checkout.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("Should create Money object with 2 decimal places and HALF_UP rounding")
    void shouldCreateMoneyWithCorrectScaleAndRounding() {
        Money m1 = Money.of(new BigDecimal("100.456"), "INR");
        assertEquals(new BigDecimal("100.46"), m1.getAmount());
        assertEquals("INR", m1.getCurrency());

        Money m2 = Money.of(new BigDecimal("100.454"), "INR");
        assertEquals(new BigDecimal("100.45"), m2.getAmount());
    }

    @Test
    @DisplayName("Should perform addition and subtraction accurately")
    void shouldPerformAddAndSubtract() {
        Money m1 = Money.of(100.50, "INR");
        Money m2 = Money.of(49.50, "INR");

        Money sum = m1.add(m2);
        assertEquals(new BigDecimal("150.00"), sum.getAmount());

        Money diff = m1.subtract(m2);
        assertEquals(new BigDecimal("51.00"), diff.getAmount());
    }

    @Test
    @DisplayName("Should perform multiplication with integer and BigDecimal factors")
    void shouldPerformMultiplication() {
        Money m1 = Money.of(99.99, "INR");
        Money total = m1.multiply(3);
        assertEquals(new BigDecimal("299.97"), total.getAmount());

        Money tax = Money.of(100.00, "INR").multiply(new BigDecimal("0.18"));
        assertEquals(new BigDecimal("18.00"), tax.getAmount());
    }

    @Test
    @DisplayName("Should reject arithmetic across different currencies")
    void shouldRejectCrossCurrencyOperations() {
        Money inr = Money.of(100.00, "INR");
        Money eur = Money.of(100.00, "EUR");

        assertThrows(IllegalArgumentException.class, () -> inr.add(eur));
    }
}
