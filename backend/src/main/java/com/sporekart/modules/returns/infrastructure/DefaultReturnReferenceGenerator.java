package com.sporekart.modules.returns.infrastructure;

import com.sporekart.modules.returns.domain.ReturnReferenceGeneratorPort;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DefaultReturnReferenceGenerator implements ReturnReferenceGeneratorPort {

    private final AtomicLong counter = new AtomicLong(100001);

    @Override
    public String generateReturnReference() {
        String uuidPrefix = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "RET-" + uuidPrefix + "-" + counter.getAndIncrement();
    }
}
