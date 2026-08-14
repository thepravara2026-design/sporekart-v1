package com.sporekart.modules.checkout.infrastructure.adapter;

import com.sporekart.modules.checkout.domain.model.Money;
import com.sporekart.modules.checkout.domain.port.TaxCalculatorPort;
import com.sporekart.modules.checkout.infrastructure.config.CheckoutProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class ConfigurableTaxAdapter implements TaxCalculatorPort {

    private final CheckoutProperties properties;

    public ConfigurableTaxAdapter(CheckoutProperties properties) {
        this.properties = Objects.requireNonNull(properties, "Checkout properties cannot be null");
    }

    @Override
    public Money calculateTax(Money taxableSubtotal, String categoryCode) {
        Objects.requireNonNull(taxableSubtotal, "Taxable subtotal cannot be null");
        BigDecimal taxRate = properties.getDefaultTaxRate();
        return taxableSubtotal.multiply(taxRate);
    }
}
