package com.sporekart.modules.checkout.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "checkout")
public class CheckoutProperties {

    private BigDecimal defaultTaxRate = new BigDecimal("0.18");
    private BigDecimal flatShippingFee = new BigDecimal("50.00");
    private BigDecimal freeShippingThreshold = new BigDecimal("1000.00");
    private String defaultCurrency = "INR";

    public BigDecimal getDefaultTaxRate() {
        return defaultTaxRate;
    }

    public void setDefaultTaxRate(BigDecimal defaultTaxRate) {
        this.defaultTaxRate = defaultTaxRate;
    }

    public BigDecimal getFlatShippingFee() {
        return flatShippingFee;
    }

    public void setFlatShippingFee(BigDecimal flatShippingFee) {
        this.flatShippingFee = flatShippingFee;
    }

    public BigDecimal getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) {
        this.freeShippingThreshold = freeShippingThreshold;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }
}
