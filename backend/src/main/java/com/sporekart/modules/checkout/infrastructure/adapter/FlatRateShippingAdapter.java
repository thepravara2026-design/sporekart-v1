package com.sporekart.modules.checkout.infrastructure.adapter;

import com.sporekart.modules.checkout.domain.model.Money;
import com.sporekart.modules.checkout.domain.port.ShippingRateProviderPort;
import com.sporekart.modules.checkout.infrastructure.config.CheckoutProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class FlatRateShippingAdapter implements ShippingRateProviderPort {

    private final CheckoutProperties properties;

    public FlatRateShippingAdapter(CheckoutProperties properties) {
        this.properties = Objects.requireNonNull(properties, "Checkout properties cannot be null");
    }

    @Override
    public Money calculateShippingEstimate(Money subtotal, String destinationAddress) {
        Objects.requireNonNull(subtotal, "Subtotal cannot be null");
        Money threshold = Money.of(properties.getFreeShippingThreshold(), subtotal.getCurrency());
        if (subtotal.equals(threshold) || subtotal.isGreaterThan(threshold)) {
            return Money.zero(subtotal.getCurrency());
        }
        return Money.of(properties.getFlatShippingFee(), subtotal.getCurrency());
    }
}
