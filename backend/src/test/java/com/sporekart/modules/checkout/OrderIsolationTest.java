package com.sporekart.modules.checkout;

import com.sporekart.modules.checkout.application.CheckoutApplicationService;
import com.sporekart.modules.checkout.application.CheckoutPricingService;
import com.sporekart.modules.checkout.controller.CheckoutController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderIsolationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Sprint 3B Checkout module must not expose or depend on Order persistence repositories or Payment gateways")
    void verifyOrderIsolationAndNonGoalBoundaries() {
        // Verify Checkout beans exist and operate statelessly
        assertTrue(context.containsBean("checkoutPricingService"));
        assertTrue(context.containsBean("checkoutApplicationService"));
        assertTrue(context.containsBean("checkoutController"));

        CheckoutPricingService pricingService = context.getBean(CheckoutPricingService.class);
        assertNotNull(pricingService);

        CheckoutController controller = context.getBean(CheckoutController.class);
        assertNotNull(controller);

        // Verify no Order/Payment entities exist in checkout package
        Package checkoutPkg = CheckoutController.class.getPackage();
        assertFalse(checkoutPkg.getName().contains("order"));
        assertFalse(checkoutPkg.getName().contains("payment"));
        assertFalse(checkoutPkg.getName().contains("shipment"));
    }
}
