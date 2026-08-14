package com.sporekart.modules.checkout.domain.port;

import com.sporekart.modules.checkout.domain.model.Money;

public interface ShippingRateProviderPort {

    Money calculateShippingEstimate(Money subtotal, String destinationAddress);
}
