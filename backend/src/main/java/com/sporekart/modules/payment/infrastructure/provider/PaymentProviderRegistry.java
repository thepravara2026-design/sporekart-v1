package com.sporekart.modules.payment.infrastructure.provider;

import com.sporekart.modules.payment.domain.PaymentProviderType;
import com.sporekart.modules.payment.domain.exception.PaymentProviderUnavailableException;
import com.sporekart.modules.payment.infrastructure.config.PaymentProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentProviderRegistry {

    private final Map<PaymentProviderType, PaymentProvider> providers;
    private final PaymentProperties paymentProperties;

    public PaymentProviderRegistry(List<PaymentProvider> providerList, PaymentProperties paymentProperties) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(PaymentProvider::getProviderType, Function.identity()));
        this.paymentProperties = paymentProperties;
    }

    public PaymentProvider getActiveProvider() {
        PaymentProviderType activeType = paymentProperties.getProvider();
        return getProvider(activeType);
    }

    public PaymentProvider getProvider(PaymentProviderType type) {
        PaymentProvider provider = providers.get(type);
        if (provider == null) {
            throw new PaymentProviderUnavailableException(type.name(), "Provider implementation bean not registered");
        }
        return provider;
    }
}
