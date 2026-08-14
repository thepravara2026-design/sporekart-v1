package com.sporekart.modules.checkout.domain.port;

import com.sporekart.modules.checkout.domain.model.Money;

public interface DiscountCalculatorPort {

    Money calculateDiscount(Money subtotal, String couponCode);
}
