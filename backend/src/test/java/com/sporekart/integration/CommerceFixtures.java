package com.sporekart.integration;

import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.cart.application.dto.AddCartItemCommand;
import com.sporekart.modules.cart.application.dto.CartDto;
import com.sporekart.modules.catalog.domain.category.CategoryStatus;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.CategoryEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import com.sporekart.modules.inventory.application.InventoryApplicationService;
import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.inventory.application.dto.ReservationDto;
import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.application.dto.AddressDto;
import com.sporekart.modules.order.application.dto.CreateOrderCommand;
import com.sporekart.modules.order.application.dto.OrderDto;
import com.sporekart.modules.payment.application.PaymentApplicationService;
import com.sporekart.modules.payment.application.dto.PaymentCheckoutDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CommerceFixtures {

    public record CheckoutResult(
            UUID orderId,
            String orderNumber,
            UUID paymentId,
            String paymentReference,
            String providerOrderId,
            BigDecimal totalAmount
    ) {}

    public static ProductEntity createTestProduct(
            SpringDataProductRepository productRepo,
            SpringDataCategoryRepository categoryRepo,
            String sku,
            String name,
            BigDecimal price,
            ProductStatus status
    ) {
        CategoryEntity category = categoryRepo.findAll().stream().findFirst().orElseGet(() -> {
            CategoryEntity cat = new CategoryEntity(
                    UUID.randomUUID(), "Test Category", "test-category-" + UUID.randomUUID().toString().substring(0, 6),
                    "Test description", CategoryStatus.ACTIVE, Instant.now(), Instant.now()
            );
            return categoryRepo.save(cat);
        });

        ProductEntity product = new ProductEntity(
                UUID.randomUUID(),
                sku,
                name,
                name + " description",
                price,
                "INR",
                status,
                category,
                Instant.now(),
                Instant.now()
        );
        return productRepo.save(product);
    }

    public static InventoryItemDto seedInventory(
            InventoryApplicationService inventoryService,
            UUID productId,
            String sku,
            int onHandQuantity
    ) {
        return inventoryService.createOrUpdateInitialStock(productId, null, sku, onHandQuantity);
    }

    public static CartDto createCartWithItem(
            CartApplicationService cartService,
            String customerId,
            UUID productId,
            int quantity
    ) {
        AddCartItemCommand command = new AddCartItemCommand(productId, null, quantity);
        return cartService.addItemToCart(customerId, command);
    }

    public static CheckoutResult executeCheckout(
            OrderApplicationService orderService,
            InventoryApplicationService inventoryService,
            PaymentApplicationService paymentService,
            String customerId,
            String idempotencyKey
    ) {
        AddressDto address = createTestAddress();
        CreateOrderCommand createCmd = new CreateOrderCommand(address, idempotencyKey, "Test delivery notes");

        // 1. Create Order
        OrderDto order = orderService.createOrder(customerId, createCmd);

        // 2. Reserve Inventory
        ReservationDto reservation = inventoryService.reserveInventoryForOrder(order.id(), customerId);

        // 3. Initiate Payment
        PaymentCheckoutDto paymentCheckout = paymentService.createPayment(order.id(), customerId);

        return new CheckoutResult(
                order.id(),
                order.orderNumber(),
                paymentCheckout.paymentId(),
                paymentCheckout.paymentReference(),
                paymentCheckout.providerOrderId(),
                order.grandTotal()
        );
    }

    public static AddressDto createTestAddress() {
        return new AddressDto(
                "John Doe",
                "+919876543210",
                "123 Tech Park",
                "Suite 4B",
                "Bengaluru",
                "Karnataka",
                "560001",
                "IND"
        );
    }
}
