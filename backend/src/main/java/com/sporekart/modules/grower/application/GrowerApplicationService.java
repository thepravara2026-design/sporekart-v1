package com.sporekart.modules.grower.application;

import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductRepository;
import com.sporekart.modules.grower.domain.GrowerProfile;
import com.sporekart.modules.grower.domain.GrowerStatus;
import com.sporekart.modules.grower.infrastructure.persistence.GrowerProfileEntity;
import com.sporekart.modules.grower.infrastructure.persistence.GrowerProfileRepository;
import com.sporekart.modules.grower.web.dto.*;
import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.inventory.infrastructure.persistence.InventoryRepository;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.infrastructure.persistence.ShipmentRepository;

import com.sporekart.modules.security.application.SecurityAuditService;
import com.sporekart.modules.security.domain.AuditEventType;
import com.sporekart.modules.security.domain.AuditStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class GrowerApplicationService {

    private static final Logger log = LoggerFactory.getLogger(GrowerApplicationService.class);

    private final GrowerProfileRepository growerProfileRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final SecurityAuditService auditService;

    public GrowerApplicationService(
            GrowerProfileRepository growerProfileRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            OrderRepository orderRepository,
            ShipmentRepository shipmentRepository
    ) {
        this(growerProfileRepository, productRepository, inventoryRepository, orderRepository, shipmentRepository, null);
    }

    @Autowired
    public GrowerApplicationService(
            GrowerProfileRepository growerProfileRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            OrderRepository orderRepository,
            ShipmentRepository shipmentRepository,
            @Autowired(required = false) SecurityAuditService auditService
    ) {
        this.growerProfileRepository = growerProfileRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.auditService = auditService;
    }

    private GrowerProfile getOrCreateProfile(String userId) {
        return growerProfileRepository.findByUserId(userId)
                .map(GrowerProfileEntity::toDomain)
                .orElseGet(() -> {
                    GrowerProfile newProfile = GrowerProfile.createDefault(userId, userId + "@sporekart.com", "Apex Spore Farms");
                    growerProfileRepository.save(GrowerProfileEntity.fromDomain(newProfile));
                    return newProfile;
                });
    }

    @Transactional(readOnly = true)
    public GrowerProfileDto getProfile(String userId) {
        return GrowerProfileDto.fromDomain(getOrCreateProfile(userId));
    }

    public GrowerProfileDto updateProfile(String userId, GrowerProfileDto dto) {
        GrowerProfile profile = getOrCreateProfile(userId);
        profile.updateProfile(dto.businessName(), dto.contactEmail(), dto.contactPhone(), dto.farmAddress());
        growerProfileRepository.save(GrowerProfileEntity.fromDomain(profile));
        log.info("GROWER_AUDIT: Grower profile updated for userId={}", userId);
        if (auditService != null) {
            auditService.logEvent(
                    AuditEventType.GROWER_PROFILE_UPDATED,
                    userId, profile.getId(), null, null,
                    AuditStatus.SUCCESS,
                    "Updated business profile: " + dto.businessName()
            );
        }
        return GrowerProfileDto.fromDomain(profile);
    }

    @Transactional(readOnly = true)
    public GrowerSettingsDto getSettings(String userId) {
        GrowerProfile profile = getOrCreateProfile(userId);
        return new GrowerSettingsDto(
                true,
                profile.getLowStockAlertThreshold(),
                profile.isAutoAcknowledgeOrders(),
                profile.getPreferredCarrier(),
                profile.getDefaultFulfillmentLocation(),
                "INR"
        );
    }

    public GrowerSettingsDto updateSettings(String userId, GrowerSettingsDto dto) {
        GrowerProfile profile = getOrCreateProfile(userId);
        profile.updateSettings(
                dto.lowStockAlertThreshold(),
                dto.autoAcknowledgeOrders(),
                dto.preferredCarrier(),
                dto.defaultFulfillmentLocation()
        );
        growerProfileRepository.save(GrowerProfileEntity.fromDomain(profile));
        log.info("GROWER_AUDIT: Grower settings updated for userId={}", userId);
        if (auditService != null) {
            auditService.logEvent(
                    AuditEventType.GROWER_SETTINGS_UPDATED,
                    userId, profile.getId(), null, null,
                    AuditStatus.SUCCESS,
                    "Updated threshold to " + dto.lowStockAlertThreshold()
            );
        }
        return getSettings(userId);
    }

    @Transactional(readOnly = true)
    public GrowerDashboardDto getDashboard(String userId) {
        List<Product> products = productRepository.findAllByGrowerId(userId);
        List<InventoryItem> inventory = inventoryRepository.findAllByGrowerId(userId);
        List<Order> orders = orderRepository.findAllByGrowerId(userId);

        BigDecimal grossRevenue = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getGrandTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int activeOrdersCount = (int) orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PROCESSING || o.getStatus() == OrderStatus.READY_FOR_FULFILMENT)
                .count();

        int lowStockCount = (int) inventory.stream()
                .filter(InventoryItem::isLowStock)
                .count();

        int activeProductsCount = (int) products.stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .count();

        int totalOrders = orders.size();
        long fulfilledOrders = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.SHIPPED)
                .count();

        double fulfillmentRate = totalOrders > 0 ? (double) fulfilledOrders / totalOrders * 100.0 : 100.0;

        return new GrowerDashboardDto(
                grossRevenue,
                activeOrdersCount,
                lowStockCount,
                activeProductsCount,
                totalOrders,
                Math.round(fulfillmentRate * 10.0) / 10.0
        );
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts(String userId) {
        return productRepository.findAllByGrowerId(userId);
    }

    @Transactional(readOnly = true)
    public Product getProductById(String userId, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + productId));
        if (product.getGrowerId() != null && !product.getGrowerId().equals(userId)) {
            throw new AccessDeniedException("Access denied: You do not own product " + productId);
        }
        return product;
    }

    public Product createProduct(String userId, CreateGrowerProductRequestDto dto) {
        Product product = Product.create(dto.sku(), dto.name(), dto.description(), dto.price(), dto.strikeOutPrice(), dto.currency(), null);
        product.setGrowerId(userId);
        product.changeStatus(ProductStatus.ACTIVE);

        com.sporekart.modules.catalog.domain.product.QuantityUnit defaultUnit = (dto.name().toLowerCase().contains("extract") || dto.name().toLowerCase().contains("liquid"))
                ? com.sporekart.modules.catalog.domain.product.QuantityUnit.L : com.sporekart.modules.catalog.domain.product.QuantityUnit.KG;
        com.sporekart.modules.catalog.domain.product.ProductVariant defaultVariant = com.sporekart.modules.catalog.domain.product.ProductVariant.create(
                product.getId(),
                Product.normalizeSku(dto.sku() + "-1" + defaultUnit.getSymbol()),
                new BigDecimal("1.00"),
                defaultUnit,
                dto.price(),
                dto.strikeOutPrice()
        );
        product.addVariant(defaultVariant);

        if (dto.imageUrls() != null && !dto.imageUrls().isEmpty()) {
            product.setImages(com.sporekart.modules.catalog.domain.product.ProductImage.listFromUrls(product.getId(), dto.imageUrls()));
        }

        Product savedProduct = productRepository.save(product);

        // Also create initial inventory item for the product variant
        UUID variantId = !savedProduct.getVariants().isEmpty() ? savedProduct.getVariants().get(0).getId() : null;
        InventoryItem item = InventoryItem.createNew(savedProduct.getId(), variantId, savedProduct.getSku(), dto.initialStockQuantity() > 0 ? dto.initialStockQuantity() : 50);
        item.setGrowerId(userId);
        inventoryRepository.save(item);

        log.info("GROWER_AUDIT: Product created for userId={}, sku={}, productId={}", userId, savedProduct.getSku(), savedProduct.getId());
        if (auditService != null) {
            auditService.logEvent(
                    AuditEventType.GROWER_PRODUCT_CREATED,
                    userId, savedProduct.getId().toString(), null, null,
                    AuditStatus.SUCCESS,
                    "Created product SKU " + savedProduct.getSku()
            );
        }

        return savedProduct;
    }

    public Product updateProduct(String userId, UUID productId, CreateGrowerProductRequestDto dto) {
        Product product = getProductById(userId, productId);
        product.updateDetails(dto.name(), dto.description(), dto.price(), dto.strikeOutPrice(), dto.currency(), null);
        if (dto.imageUrls() != null) {
            product.setImages(com.sporekart.modules.catalog.domain.product.ProductImage.listFromUrls(productId, dto.imageUrls()));
        }
        Product updated = productRepository.save(product);
        log.info("GROWER_AUDIT: Product updated for userId={}, productId={}", userId, productId);
        if (auditService != null) {
            auditService.logEvent(
                    AuditEventType.GROWER_PRODUCT_UPDATED,
                    userId, productId.toString(), null, null,
                    AuditStatus.SUCCESS,
                    "Updated product details for " + productId
            );
        }
        return updated;
    }

    /**
     * Replaces the full ordered image set of a product. Growers may only manage
     * images of products they own; admins may manage any product.
     */
    public Product updateProductImages(String userId, UUID productId, List<String> imageUrls, boolean isAdmin) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + productId));
        if (!isAdmin && product.getGrowerId() != null && !product.getGrowerId().equals(userId)) {
            throw new AccessDeniedException("Access denied: You do not own product " + productId);
        }
        product.setImages(com.sporekart.modules.catalog.domain.product.ProductImage.listFromUrls(productId, imageUrls));
        Product updated = productRepository.save(product);
        log.info("GROWER_AUDIT: Product images updated for userId={}, productId={}, count={}", userId, productId, imageUrls == null ? 0 : imageUrls.size());
        if (auditService != null) {
            auditService.logEvent(
                    AuditEventType.GROWER_PRODUCT_UPDATED,
                    userId, productId.toString(), null, null,
                    AuditStatus.SUCCESS,
                    "Updated product images for " + productId
            );
        }
        return updated;
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> getInventory(String userId) {
        return inventoryRepository.findAllByGrowerId(userId);
    }

    @Transactional(readOnly = true)
    public InventoryItem getInventoryBySku(String userId, String sku) {
        InventoryItem item = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new NoSuchElementException("Inventory item not found for SKU: " + sku));
        if (item.getGrowerId() != null && !item.getGrowerId().equals(userId)) {
            throw new AccessDeniedException("Access denied: You do not own inventory SKU " + sku);
        }
        return item;
    }

    public InventoryItem adjustStock(String userId, String sku, AdjustStockRequestDto dto) {
        InventoryItem item = getInventoryBySku(userId, sku);
        int oldQuantity = item.getOnHandQuantity();
        item.adjustOnHand(dto.newOnHandQuantity());
        InventoryItem updated = inventoryRepository.save(item);
        log.info("GROWER_AUDIT: Stock adjusted for userId={}, sku={}, oldQty={}, newQty={}", userId, sku, oldQuantity, dto.newOnHandQuantity());
        if (auditService != null) {
            auditService.logEvent(
                    AuditEventType.GROWER_STOCK_ADJUSTED,
                    userId, item.getId().toString(), null, null,
                    AuditStatus.SUCCESS,
                    "Adjusted SKU " + sku + " from " + oldQuantity + " to " + dto.newOnHandQuantity()
            );
        }
        return updated;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders(String userId) {
        return orderRepository.findAllByGrowerId(userId);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(String userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + orderId));
        if (order.getGrowerId() != null && !order.getGrowerId().equals(userId)) {
            throw new AccessDeniedException("Access denied: You do not own order " + orderId);
        }
        return order;
    }

    public Order transitionOrder(String userId, UUID orderId, OrderStatus newStatus) {
        Order order = getOrderById(userId, orderId);
        OrderStatus oldStatus = order.getStatus();
        switch (newStatus) {
            case PROCESSING -> order.startProcessing();
            case READY_FOR_FULFILMENT -> order.markReadyForFulfilment();
            case SHIPPED -> order.markShipped();
            case DELIVERED -> order.markDelivered();
            case COMPLETED -> order.markCompleted();
            case CANCELLED -> order.cancel();
            default -> throw new IllegalArgumentException("Unsupported order status transition to: " + newStatus);
        }
        Order saved = orderRepository.save(order);
        log.info("GROWER_AUDIT: Order status transitioned for userId={}, orderId={}, from={}, to={}", userId, orderId, oldStatus, newStatus);
        if (auditService != null) {
            auditService.logEvent(
                    AuditEventType.GROWER_ORDER_TRANSITIONED,
                    userId, orderId.toString(), null, null,
                    AuditStatus.SUCCESS,
                    "Transitioned order " + orderId + " from " + oldStatus + " to " + newStatus
            );
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Shipment> getShipments(String userId) {
        return shipmentRepository.findAllByGrowerId(userId);
    }

    @Transactional(readOnly = true)
    public GrowerReportSummaryDto getReportSummary(String userId, String period) {
        List<Order> orders = orderRepository.findAllByGrowerId(userId);

        BigDecimal totalSales = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getGrandTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalOrders = orders.size();
        int unitsSold = orders.stream()
                .mapToInt(o -> o.getItems().stream().mapToInt(item -> item.getQuantity()).sum())
                .sum();

        long fulfilledOrders = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.SHIPPED)
                .count();

        double fulfillmentRate = totalOrders > 0 ? (double) fulfilledOrders / totalOrders * 100.0 : 100.0;
        BigDecimal avgOrderValue = totalOrders > 0 ? totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return new GrowerReportSummaryDto(
                period != null ? period : "THIS_MONTH",
                totalSales,
                totalOrders,
                unitsSold,
                Math.round(fulfillmentRate * 10.0) / 10.0,
                avgOrderValue,
                0.0
        );
    }
}
