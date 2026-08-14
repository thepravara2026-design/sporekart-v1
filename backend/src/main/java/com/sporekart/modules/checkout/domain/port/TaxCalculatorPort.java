package com.sporekart.modules.checkout.domain.port;

import com.sporekart.modules.checkout.domain.model.Money;

public interface TaxCalculatorPort {

    Money calculateTax(Money taxableSubtotal, String categoryCode);
}
