package com.sporekart.modules.checkout.application;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartItem;
import com.sporekart.modules.cart.domain.CartStatus;
import com.sporekart.modules.checkout.domain.exception.CartEmptyException;
import com.sporekart.modules.checkout.domain.exception.CheckoutNotEligibleException;
import com.sporekart.modules.checkout.domain.exception.CurrencyMismatchException;
import com.sporekart.modules.checkout.domain.model.CheckoutLineItem;
import com.sporekart.modules.checkout.domain.model.CheckoutPreview;
import com.sporekart.modules.checkout.domain.model.CheckoutWarning;
import com.sporekart.modules.checkout.domain.model.Money;
import com.sporekart.modules.checkout.domain.port.AvailabilityPort;
import com.sporekart.modules.checkout.domain.port.DiscountCalculatorPort;
import com.sporekart.modules.checkout.domain.port.ShippingRateProviderPort;
import com.sporekart.modules.checkout.domain.port.TaxCalculatorPort;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CheckoutPricingService {

    private final AvailabilityPort availabilityPort;
    private final TaxCalculatorPort taxCalculatorPort;
    private final ShippingRateProviderPort shippingRateProviderPort;
    private final DiscountCalculatorPort discountCalculatorPort;

    public CheckoutPricingService(
            AvailabilityPort availabilityPort,
            TaxCalculatorPort taxCalculatorPort,
            ShippingRateProviderPort shippingRateProviderPort,
            DiscountCalculatorPort discountCalculatorPort
    ) {
        this.availabilityPort = Objects.requireNonNull(availabilityPort, "Availability port cannot be null");
        this.taxCalculatorPort = Objects.requireNonNull(taxCalculatorPort, "Tax calculator port cannot be null");
        this.shippingRateProviderPort = Objects.requireNonNull(shippingRateProviderPort, "Shipping rate provider port cannot be null");
        this.discountCalculatorPort = Objects.requireNonNull(discountCalculatorPort, "Discount calculator port cannot be null");
    }

    public CheckoutPreview calculateCheckoutPreview(Cart cart, String destinationAddress, String couponCode) {
        Objects.requireNonNull(cart, "Cart cannot be null");

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new CheckoutNotEligibleException("Cart status must be ACTIVE for checkout preview, but found: " + cart.getStatus());
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartEmptyException(cart.getId());
        }

        String cartCurrency = cart.getCurrency();
        List<CheckoutLineItem> lineItems = new ArrayList<>();
        List<CheckoutWarning> warnings = new ArrayList<>();

        Money subtotal = Money.zero(cartCurrency);
        Money discountTotal = Money.zero(cartCurrency);
        Money taxTotal = Money.zero(cartCurrency);

        for (CartItem item : cart.getItems()) {
            AvailabilityPort.CatalogItemDetails catalogItem = availabilityPort.getAuthoritativeItemDetails(item.getProductId());

            if (!catalogItem.purchasable()) {
                throw new CheckoutNotEligibleException(
                        "Product '" + catalogItem.name() + "' (ID: " + item.getProductId() + ") is no longer purchasable"
                );
            }

            if (!cartCurrency.equalsIgnoreCase(catalogItem.currency())) {
                throw new CurrencyMismatchException(cartCurrency, catalogItem.currency());
            }

            Money snapshotPrice = Money.of(item.getUnitPriceSnapshot(), cartCurrency);
            Money authoritativePrice = Money.of(catalogItem.price(), cartCurrency);

            boolean priceChanged = !snapshotPrice.equals(authoritativePrice);
            if (priceChanged) {
                warnings.add(new CheckoutWarning(
                        CheckoutWarning.WarningType.PRICE_CHANGED,
                        item.getProductId(),
                        "Price for '" + catalogItem.name() + "' changed from " + snapshotPrice + " to " + authoritativePrice
                ));
            }

            Money lineSubtotal = authoritativePrice.multiply(item.getQuantity());
            Money lineDiscount = discountCalculatorPort.calculateDiscount(lineSubtotal, couponCode);
            Money taxableAmount = lineSubtotal.subtract(lineDiscount);
            Money lineTax = taxCalculatorPort.calculateTax(taxableAmount, null);
            Money lineTotal = lineSubtotal.subtract(lineDiscount).add(lineTax);

            CheckoutLineItem calculatedLine = new CheckoutLineItem(
                    item.getId(),
                    item.getProductId(),
                    catalogItem.sku(),
                    catalogItem.name(),
                    item.getQuantity(),
                    snapshotPrice,
                    authoritativePrice,
                    priceChanged,
                    lineSubtotal,
                    lineDiscount,
                    lineTax,
                    lineTotal
            );

            lineItems.add(calculatedLine);
            subtotal = subtotal.add(lineSubtotal);
            discountTotal = discountTotal.add(lineDiscount);
            taxTotal = taxTotal.add(lineTax);
        }

        Money shippingFee = shippingRateProviderPort.calculateShippingEstimate(subtotal, destinationAddress);
        Money grandTotal = subtotal.subtract(discountTotal).add(taxTotal).add(shippingFee);

        if (grandTotal.isNegative()) {
            grandTotal = Money.zero(cartCurrency);
        }

        return new CheckoutPreview(
                UUID.randomUUID(),
                cart.getId(),
                cart.getCustomerId(),
                cartCurrency,
                lineItems,
                subtotal,
                discountTotal,
                taxTotal,
                shippingFee,
                grandTotal,
                warnings,
                OffsetDateTime.now()
        );
    }
}
