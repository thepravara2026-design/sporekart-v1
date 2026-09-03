package com.sporekart.modules.grower;

import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductRepository;
import com.sporekart.modules.grower.application.GrowerApplicationService;
import com.sporekart.modules.grower.domain.GrowerProfile;
import com.sporekart.modules.grower.domain.GrowerStatus;
import com.sporekart.modules.grower.infrastructure.persistence.GrowerProfileEntity;
import com.sporekart.modules.grower.infrastructure.persistence.GrowerProfileRepository;
import com.sporekart.modules.grower.web.dto.AdjustStockRequestDto;
import com.sporekart.modules.grower.web.dto.CreateGrowerProductRequestDto;
import com.sporekart.modules.grower.web.dto.GrowerProfileDto;
import com.sporekart.modules.grower.web.dto.GrowerSettingsDto;
import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.inventory.infrastructure.persistence.InventoryRepository;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import com.sporekart.modules.shipment.infrastructure.persistence.ShipmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrowerProductionReadinessTest {

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

    @Mock
    private SecurityAuditService auditService;

    private GrowerApplicationService growerService;

    private final String growerId = "grower-prod-1";

    @BeforeEach
    void setUp() {
        growerService = new GrowerApplicationService(
                growerProfileRepository,
                productRepository,
                inventoryRepository,
                orderRepository,
                shipmentRepository,
                auditService
        );
    }

    @Test
    @DisplayName("AUDITABILITY #1: Profile update emits SecurityAuditService event")
    void testProfileUpdateAuditLogging() {
        GrowerProfile profile = new GrowerProfile(
                "profile-1", growerId, "Old Name", "grower@sporekart.com",
                "+1-555-0100", "100 Main St", GrowerStatus.ACTIVE, 5, false,
                "Carrier", "Location", OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(growerProfileRepository.findByUserId(growerId)).thenReturn(Optional.of(GrowerProfileEntity.fromDomain(profile)));

        var updateDto = new GrowerProfileDto("profile-1", growerId, "New Farm Name", "grower@sporekart.com", "+1-555-0100", "100 Main St", GrowerStatus.ACTIVE, 5, false, "Carrier", "Location");
        growerService.updateProfile(growerId, updateDto);

        verify(auditService, times(1)).logEvent(
                eq(AuditEventType.GROWER_PROFILE_UPDATED),
                eq(growerId),
                anyString(),
                eq(null),
                eq(null),
                eq(AuditStatus.SUCCESS),
                contains("New Farm Name")
        );
    }

    @Test
    @DisplayName("AUDITABILITY #2: Settings update emits SecurityAuditService event")
    void testSettingsUpdateAuditLogging() {
        GrowerProfile profile = new GrowerProfile(
                "profile-1", growerId, "Farm Name", "grower@sporekart.com",
                "+1-555-0100", "100 Main St", GrowerStatus.ACTIVE, 5, false,
                "Carrier", "Location", OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(growerProfileRepository.findByUserId(growerId)).thenReturn(Optional.of(GrowerProfileEntity.fromDomain(profile)));

        var settingsDto = new GrowerSettingsDto(true, 10, true, "Express", "Lab A", "INR");
        growerService.updateSettings(growerId, settingsDto);

        verify(auditService, times(1)).logEvent(
                eq(AuditEventType.GROWER_SETTINGS_UPDATED),
                eq(growerId),
                anyString(),
                eq(null),
                eq(null),
                eq(AuditStatus.SUCCESS),
                contains("10")
        );
    }

    @Test
    @DisplayName("AUDITABILITY #3: Stock adjustment emits SecurityAuditService event")
    void testStockAdjustmentAuditLogging() {
        String sku = "SKU-TEST-100";
        InventoryItem item = InventoryItem.createNew(UUID.randomUUID(), null, sku, 40);
        item.setGrowerId(growerId);

        when(inventoryRepository.findBySku(sku)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        var adjustDto = new AdjustStockRequestDto(80, "RESTOCK");
        growerService.adjustStock(growerId, sku, adjustDto);

        verify(auditService, times(1)).logEvent(
                eq(AuditEventType.GROWER_STOCK_ADJUSTED),
                eq(growerId),
                eq(item.getId().toString()),
                eq(null),
                eq(null),
                eq(AuditStatus.SUCCESS),
                contains("Adjusted SKU " + sku + " from 40 to 80")
        );
    }

    @Test
    @DisplayName("AUDITABILITY #4: Order status transition emits SecurityAuditService event")
    void testOrderTransitionAuditLogging() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.createNewOrder(
                "ORD-AUDIT-1", "cust-1", "INR", new BigDecimal("100.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100.00"),
                "idemp-1", new com.sporekart.modules.order.domain.AddressSnapshot("John", "555-0100", "Line1", "Line2", "City", "State", "12345", "USA"),
                "Notes", List.of()
        );
        order.setGrowerId(growerId);
        order.markPaid();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        growerService.transitionOrder(growerId, orderId, OrderStatus.PROCESSING);

        verify(auditService, times(1)).logEvent(
                eq(AuditEventType.GROWER_ORDER_TRANSITIONED),
                eq(growerId),
                eq(orderId.toString()),
                eq(null),
                eq(null),
                eq(AuditStatus.SUCCESS),
                contains("PROCESSING")
        );
    }
}
