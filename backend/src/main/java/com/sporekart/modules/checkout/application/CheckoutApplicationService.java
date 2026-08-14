package com.sporekart.modules.checkout.application;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartStatus;
import com.sporekart.modules.cart.domain.exception.CartNotFoundException;
import com.sporekart.modules.cart.infrastructure.persistence.CartRepository;
import com.sporekart.modules.checkout.application.dto.CheckoutPreviewRequest;
import com.sporekart.modules.checkout.application.dto.CheckoutPreviewResponse;
import com.sporekart.modules.checkout.domain.model.CheckoutPreview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class CheckoutApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutApplicationService.class);

    private final CartRepository cartRepository;
    private final CheckoutPricingService checkoutPricingService;

    public CheckoutApplicationService(
            CartRepository cartRepository,
            CheckoutPricingService checkoutPricingService
    ) {
        this.cartRepository = Objects.requireNonNull(cartRepository, "Cart repository cannot be null");
        this.checkoutPricingService = Objects.requireNonNull(checkoutPricingService, "Checkout pricing service cannot be null");
    }

    public CheckoutPreviewResponse generateCheckoutPreview(String customerId, CheckoutPreviewRequest request) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer identity cannot be null or blank");
        }

        Cart activeCart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new CartNotFoundException(customerId));

        String destinationAddress = request != null ? request.destinationAddress() : null;
        String couponCode = request != null ? request.couponCode() : null;

        CheckoutPreview preview = checkoutPricingService.calculateCheckoutPreview(activeCart, destinationAddress, couponCode);
        log.info("Generated checkout preview {} for customer {} (cart: {}, grandTotal: {})",
                preview.getPreviewId(), customerId, activeCart.getId(), preview.getGrandTotal());

        return CheckoutPreviewResponse.fromDomain(preview);
    }
}
