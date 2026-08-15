package com.sporekart.modules.shipment.infrastructure;

import com.sporekart.modules.shipment.domain.ShipmentReferenceGeneratorPort;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DefaultShipmentReferenceGenerator implements ShipmentReferenceGeneratorPort {
    private final AtomicLong counter = new AtomicLong(100000);

    @Override
    public String generateReference() {
        long current = counter.incrementAndGet();
        String uuidPart = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return String.format("SHP-%s-%d", uuidPart, current);
    }
}
