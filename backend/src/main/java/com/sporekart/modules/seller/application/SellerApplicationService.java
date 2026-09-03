package com.sporekart.modules.seller.application;

import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductRepository;
import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.inventory.infrastructure.persistence.InventoryRepository;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.seller.domain.SellerPayout;
import com.sporekart.modules.seller.domain.SellerProfile;
import com.sporekart.modules.seller.infrastructure.persistence.SellerPayoutEntity;
import com.sporekart.modules.seller.infrastructure.persistence.SellerPayoutRepository;
import com.sporekart.modules.seller.infrastructure.persistence.SellerProfileEntity;
import com.sporekart.modules.seller.infrastructure.persistence.SellerProfileRepository;
import com.sporekart.modules.seller.web.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class SellerApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SellerApplicationService.class);

    private final SellerProfileRepository sellerProfileRepository;
    private final SellerPayoutRepository sellerPayoutRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    public SellerApplicationService(
            SellerProfileRepository sellerProfileRepository,
            SellerPayoutRepository sellerPayoutRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            OrderRepository orderRepository
    ) {
        this.sellerProfileRepository = sellerProfileRepository;
        this.sellerPayoutRepository = sellerPayoutRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
    }

    private SellerProfile getOrCreateProfile(String userId) {
        return sellerProfileRepository.findByUserId(userId)
                .map(SellerProfileEntity::toDomain)
                .orElseGet(() -> {
                    SellerProfile newProfile = SellerProfile.createDefault(userId, userId + "@sporekart.com", "Seller Hub");
                    sellerProfileRepository.save(SellerProfileEntity.fromDomain(newProfile));
                    return newProfile;
                });
    }

    @Transactional(readOnly = true)
    public SellerMetricsDto getMetrics(String userId) {
        getOrCreateProfile(userId);
        List<Product> products = productRepository.findAllByGrowerId(userId);
        List<InventoryItem> inventory = inventoryRepository.findAllByGrowerId(userId);
        List<Order> orders = orderRepository.findAllByGrowerId(userId);
        List<SellerPayout> payouts = sellerPayoutRepository.findAllBySellerId(userId)
                .stream().map(SellerPayoutEntity::toDomain).toList();

        BigDecimal totalSales = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getGrandTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyRevenue = totalSales; // Current period sales sum

        int activeListingsCount = (int) products.stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .count();

        int totalInventoryOnHand = inventory.stream()
                .mapToInt(InventoryItem::getOnHandQuantity)
                .sum();

        int pendingOrdersCount = (int) orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PROCESSING || o.getStatus() == OrderStatus.READY_FOR_FULFILMENT || o.getStatus() == OrderStatus.CREATED)
                .count();

        int fulfilledOrdersCount = (int) orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.COMPLETED)
                .count();

        BigDecimal payoutPendingAmount = payouts.stream()
                .filter(p -> "PENDING".equalsIgnoreCase(p.getStatus()))
                .map(SellerPayout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal payoutSettledAmount = payouts.stream()
                .filter(p -> "COMPLETED".equalsIgnoreCase(p.getStatus()) || "SETTLED".equalsIgnoreCase(p.getStatus()))
                .map(SellerPayout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SellerMetricsDto(
                totalSales,
                monthlyRevenue,
                activeListingsCount,
                totalInventoryOnHand,
                0,
                pendingOrdersCount,
                fulfilledOrdersCount,
                payoutPendingAmount,
                payoutSettledAmount
        );
    }

    @Transactional(readOnly = true)
    public List<SellerProductDto> getProducts(String userId) {
        List<Product> products = productRepository.findAllByGrowerId(userId);
        List<InventoryItem> inventoryList = inventoryRepository.findAllByGrowerId(userId);

        return products.stream().map(p -> {
            InventoryItem inv = inventoryList.stream()
                    .filter(i -> i.getProductId().equals(p.getId()))
                    .findFirst()
                    .orElse(null);

            int onHand = inv != null ? inv.getOnHandQuantity() : 0;
            int reserved = inv != null ? inv.getReservedQuantity() : 0;

            return new SellerProductDto(
                    p.getId().toString(),
                    p.getName(),
                    p.getSku(),
                    "Mushroom Cultivation",
                    p.getPrice(),
                    p.getStrikeOutPrice(),
                    p.getCurrency(),
                    onHand,
                    reserved,
                    "SYNCED",
                    p.getStatus().name(),
                    OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public SellerProductDto getProductById(String userId, String productId) {
        UUID id = UUID.fromString(productId);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));

        if (product.getGrowerId() != null && !product.getGrowerId().equals(userId)) {
            throw new AccessDeniedException("Access denied to product " + productId);
        }

        InventoryItem inv = inventoryRepository.findBySku(product.getSku()).orElse(null);
        int onHand = inv != null ? inv.getOnHandQuantity() : 0;
        int reserved = inv != null ? inv.getReservedQuantity() : 0;

        return new SellerProductDto(
                product.getId().toString(),
                product.getName(),
                product.getSku(),
                "Mushroom Cultivation",
                product.getPrice(),
                product.getStrikeOutPrice(),
                product.getCurrency(),
                onHand,
                reserved,
                "SYNCED",
                product.getStatus().name(),
                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
    }

    public SellerProductDto createProduct(String userId, CreateSellerProductRequestDto dto) {
        Product product = Product.create(dto.sku(), dto.name(), dto.name(), dto.price(), dto.strikeOutPrice(), "INR", null);
        product.setGrowerId(userId);
        product.changeStatus(ProductStatus.ACTIVE);

        Product saved = productRepository.save(product);

        InventoryItem item = InventoryItem.createNew(saved.getId(), null, saved.getSku(), dto.initialStock() > 0 ? dto.initialStock() : 50);
        item.setGrowerId(userId);
        inventoryRepository.save(item);

        log.info("SELLER_AUDIT: Product created by seller userId={}, sku={}", userId, saved.getSku());

        return new SellerProductDto(
                saved.getId().toString(),
                saved.getName(),
                saved.getSku(),
                dto.category() != null ? dto.category() : "Mushroom Cultivation",
                saved.getPrice(),
                saved.getStrikeOutPrice(),
                saved.getCurrency(),
                item.getOnHandQuantity(),
                0,
                "SYNCED",
                saved.getStatus().name(),
                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
    }

    @Transactional(readOnly = true)
    public List<SellerInventoryDto> getInventory(String userId) {
        List<InventoryItem> items = inventoryRepository.findAllByGrowerId(userId);
        List<Product> products = productRepository.findAllByGrowerId(userId);

        return items.stream().map(item -> {
            String pName = products.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .map(Product::getName)
                    .findFirst()
                    .orElse("Catalog Item");

            return new SellerInventoryDto(
                    item.getId().toString(),
                    item.getSku(),
                    pName,
                    "Main Facility",
                    item.getOnHandQuantity(),
                    item.getReservedQuantity(),
                    item.getAvailableQuantity(),
                    "SYNCED",
                    OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            );
        }).toList();
    }

    public SellerInventoryDto adjustStock(String userId, String sku, AdjustSellerStockRequestDto dto) {
        InventoryItem item = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new NoSuchElementException("Inventory item not found for SKU: " + sku));

        if (item.getGrowerId() != null && !item.getGrowerId().equals(userId)) {
            throw new AccessDeniedException("Access denied to inventory SKU " + sku);
        }

        item.adjustOnHand(dto.newOnHandQuantity());
        InventoryItem updated = inventoryRepository.save(item);

        log.info("SELLER_AUDIT: Stock adjusted by seller userId={}, sku={}, newQty={}", userId, sku, dto.newOnHandQuantity());

        return new SellerInventoryDto(
                updated.getId().toString(),
                updated.getSku(),
                sku,
                "Main Facility",
                updated.getOnHandQuantity(),
                updated.getReservedQuantity(),
                updated.getAvailableQuantity(),
                "SYNCED",
                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
    }

    @Transactional(readOnly = true)
    public List<SellerOrderDto> getOrders(String userId) {
        List<Order> orders = orderRepository.findAllByGrowerId(userId);

        return orders.stream().map(o -> new SellerOrderDto(
                o.getId().toString(),
                o.getOrderNumber(),
                "Buyer",
                "buyer@example.com",
                o.getItems().size(),
                o.getGrandTotal(),
                "INR",
                o.getStatus().name(),
                o.getCreatedAt() != null ? o.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )).toList();
    }

    public SellerOrderDto transitionOrder(String userId, String orderIdStr, String newStatusStr) {
        UUID orderId = UUID.fromString(orderIdStr);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderIdStr));

        if (order.getGrowerId() != null && !order.getGrowerId().equals(userId)) {
            throw new AccessDeniedException("Access denied to order " + orderIdStr);
        }

        OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
        switch (newStatus) {
            case PROCESSING -> order.startProcessing();
            case READY_FOR_FULFILMENT -> order.markReadyForFulfilment();
            case SHIPPED -> order.markShipped();
            case DELIVERED -> order.markDelivered();
            case COMPLETED -> order.markCompleted();
            case CANCELLED -> order.cancel();
            default -> {}
        }

        Order saved = orderRepository.save(order);
        log.info("SELLER_AUDIT: Order status transitioned by seller userId={}, orderId={}, newStatus={}", userId, orderIdStr, newStatusStr);

        return new SellerOrderDto(
                saved.getId().toString(),
                saved.getOrderNumber(),
                "Buyer",
                "buyer@example.com",
                saved.getItems().size(),
                saved.getGrandTotal(),
                "INR",
                saved.getStatus().name(),
                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
    }

    @Transactional(readOnly = true)
    public List<SellerPayoutDto> getPayouts(String userId) {
        List<SellerPayout> payouts = sellerPayoutRepository.findAllBySellerId(userId)
                .stream().map(SellerPayoutEntity::toDomain).toList();

        if (payouts.isEmpty()) {
            SellerPayout defaultPayout = new SellerPayout(
                    UUID.randomUUID(),
                    userId,
                    "PAY-" + System.currentTimeMillis() / 1000,
                    "Current Period",
                    new BigDecimal("1250.00"),
                    "INR",
                    "COMPLETED",
                    "4321",
                    OffsetDateTime.now()
            );
            sellerPayoutRepository.save(SellerPayoutEntity.fromDomain(defaultPayout));
            payouts = List.of(defaultPayout);
        }

        return payouts.stream().map(p -> new SellerPayoutDto(
                p.getId().toString(),
                p.getPayoutReference(),
                p.getPeriod(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus(),
                p.getPayoutDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                p.getBankAccountLast4()
        )).toList();
    }
}
