package com.sporekart.modules.checkout.infrastructure.adapter;

import com.sporekart.modules.checkout.domain.model.Money;
import com.sporekart.modules.checkout.domain.port.DiscountCalculatorPort;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class NoOpDiscountAdapter implements DiscountCalculatorPort {

    @Override
    public Money calculateDiscount(Money subtotal, String couponCode) {
        Objects.requireNonNull(subtotal, "Subtotal cannot be null");
        return Money.zero(subtotal.getCurrency());
    }
}
