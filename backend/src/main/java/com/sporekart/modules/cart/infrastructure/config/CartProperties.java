package com.sporekart.modules.cart.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cart")
public class CartProperties {

    private int maxItemQuantity = 50;
    private int maxTotalItems = 100;

    public int getMaxItemQuantity() {
        return maxItemQuantity;
    }

    public void setMaxItemQuantity(int maxItemQuantity) {
        this.maxItemQuantity = maxItemQuantity;
    }

    public int getMaxTotalItems() {
        return maxTotalItems;
    }

    public void setMaxTotalItems(int maxTotalItems) {
        this.maxTotalItems = maxTotalItems;
    }
}
