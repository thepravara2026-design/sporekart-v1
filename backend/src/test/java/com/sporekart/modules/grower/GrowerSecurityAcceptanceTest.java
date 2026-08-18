package com.sporekart.modules.grower;

import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductRepository;
import com.sporekart.modules.grower.application.GrowerApplicationService;
import com.sporekart.modules.grower.domain.GrowerProfile;
import com.sporekart.modules.grower.domain.GrowerStatus;
import com.sporekart.modules.grower.infrastructure.persistence.GrowerProfileEntity;
import com.sporekart.modules.grower.infrastructure.persistence.GrowerProfileRepository;
import com.sporekart.modules.grower.web.dto.GrowerProfileDto;
import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.inventory.infrastructure.persistence.InventoryRepository;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.shipment.infrastructure.persistence.ShipmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrowerSecurityAcceptanceTest {

    @Mock
    private GrowerProfileRepository growerProfileRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    private GrowerApplicationService growerService;

    private final String growerA = "grower-1";
    private final String growerB = "grower-2";

    @BeforeEach
    void setUp() {
        growerService = new GrowerApplicationService(
                growerProfileRepository,
                productRepository,
                inventoryRepository,
                orderRepository,
                shipmentRepository
        );
    }

    @Test
    @DisplayName("SCENARIO #1: Grower A accesses own profile & products -> SUCCESS")
    void scenario1_growerA_accesses_own_resources() {
        GrowerProfile profileA = new GrowerProfile(
                "profile-1", growerA, "Grower A Farm", "growerA@sporekart.com",
                "+1-555-0199", "100 Farm Way", GrowerStatus.ACTIVE, 5, false,
                "Standard Express", "Main Lab", OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(growerProfileRepository.findByUserId(growerA)).thenReturn(Optional.of(GrowerProfileEntity.fromDomain(profileA)));

        Product prodA = Product.create("SKU-A", "Product A", "Desc A", new BigDecimal("25.00"), "USD", null);
        prodA.setGrowerId(growerA);
        when(productRepository.findAllByGrowerId(growerA)).thenReturn(List.of(prodA));

        GrowerProfileDto profileResult = growerService.getProfile(growerA);
        List<Product> productsResult = growerService.getProducts(growerA);

        assertNotNull(profileResult);
        assertEquals("Grower A Farm", profileResult.businessName());
        assertEquals(1, productsResult.size());
        assertEquals(growerA, productsResult.get(0).getGrowerId());
    }

    @Test
    @DisplayName("SCENARIO #2: Grower A attempts to access Grower B product -> DENIED (AccessDeniedException)")
    void scenario2_growerA_accesses_growerB_product_denied() {
        UUID prodIdB = UUID.randomUUID();
        Product prodB = Product.create("SKU-B", "Product B", "Desc B", new BigDecimal("35.00"), "USD", null);
        prodB.setGrowerId(growerB);

        when(productRepository.findById(prodIdB)).thenReturn(Optional.of(prodB));

        assertThrows(AccessDeniedException.class, () ->
                growerService.getProductById(growerA, prodIdB)
        );
    }

    @Test
    @DisplayName("SCENARIO #3: Grower A attempts to access Grower B inventory -> DENIED (AccessDeniedException)")
    void scenario3_growerA_accesses_growerB_inventory_denied() {
        String skuB = "SKU-PROD-B";
        InventoryItem itemB = InventoryItem.createNew(UUID.randomUUID(), null, skuB, 100);
        itemB.setGrowerId(growerB);

        when(inventoryRepository.findBySku(skuB)).thenReturn(Optional.of(itemB));

        assertThrows(AccessDeniedException.class, () ->
                growerService.getInventoryBySku(growerA, skuB)
        );
    }

    @Test
    @DisplayName("SCENARIO #4: Grower A attempts to access Grower B order -> DENIED (AccessDeniedException)")
    void scenario4_growerA_accesses_growerB_order_denied() {
        Order orderB = Order.createNewOrder(
                "ORD-GROWER-B",
                "cust-1",
                "INR",
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                "idemp-1",
                new com.sporekart.modules.order.domain.AddressSnapshot("John", "555-0100", "Line1", "Line2", "City", "State", "12345", "Country"),
                "Notes",
                List.of()
        );
        orderB.setGrowerId(growerB);
        UUID orderIdB = orderB.getId();

        when(orderRepository.findById(orderIdB)).thenReturn(Optional.of(orderB));

        assertThrows(AccessDeniedException.class, () ->
                growerService.getOrderById(growerA, orderIdB)
        );
    }

    @Test
    @DisplayName("SCENARIO #5: Server-side identity resolution ignores spoofed request parameters")
    void scenario5_server_side_identity_resolution() {
        growerService.getDashboard(growerA);

        verify(productRepository, times(1)).findAllByGrowerId(growerA);
        verify(productRepository, never()).findAllByGrowerId(growerB);
        verify(orderRepository, times(1)).findAllByGrowerId(growerA);
        verify(orderRepository, never()).findAllByGrowerId(growerB);
    }
}
