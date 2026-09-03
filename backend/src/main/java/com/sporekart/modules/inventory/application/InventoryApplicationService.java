package com.sporekart.modules.inventory.application;

import com.sporekart.modules.inventory.application.dto.InventoryItemDto;
import com.sporekart.modules.inventory.application.dto.ReservationDto;
import com.sporekart.modules.inventory.application.dto.StockAdjustmentCommand;
import com.sporekart.modules.inventory.application.dto.StockAvailabilityDto;
import com.sporekart.modules.inventory.application.dto.StockMovementDto;
import com.sporekart.modules.inventory.domain.InventoryItem;
import com.sporekart.modules.inventory.domain.MovementType;
import com.sporekart.modules.inventory.domain.ReservationStatus;
import com.sporekart.modules.inventory.domain.StockMovement;
import com.sporekart.modules.inventory.domain.StockReservation;
import com.sporekart.modules.inventory.domain.StockReservationItem;
import com.sporekart.modules.inventory.domain.exception.InsufficientStockException;
import com.sporekart.modules.inventory.domain.exception.InventoryItemNotFoundException;
import com.sporekart.modules.inventory.domain.exception.ReservationNotFoundException;
import com.sporekart.modules.inventory.infrastructure.config.InventoryProperties;
import com.sporekart.modules.inventory.infrastructure.persistence.InventoryRepository;
import com.sporekart.modules.inventory.infrastructure.persistence.ReservationRepository;
import com.sporekart.modules.inventory.infrastructure.persistence.StockMovementRepository;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.exception.OrderNotFoundException;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InventoryApplicationService {

    private static final Logger log = LoggerFactory.getLogger(InventoryApplicationService.class);

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;
    private final StockMovementRepository stockMovementRepository;
    private final OrderRepository orderRepository;
    private final InventoryProperties inventoryProperties;

    public InventoryApplicationService(
            InventoryRepository inventoryRepository,
            ReservationRepository reservationRepository,
            StockMovementRepository stockMovementRepository,
            OrderRepository orderRepository,
            InventoryProperties inventoryProperties
    ) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.orderRepository = orderRepository;
        this.inventoryProperties = inventoryProperties;
    }

    @Transactional
    public ReservationDto reserveInventoryForOrder(UUID orderId, String customerId) {
        log.info("Attempting stock reservation for orderId: {}", orderId);

        // 1. Idempotency Check: return existing active reservation if present
        Optional<StockReservation> existingOpt = reservationRepository.findByOrderId(orderId);
        if (existingOpt.isPresent()) {
            StockReservation existing = existingOpt.get();
            if (existing.isActive()) {
                log.info("Idempotent replay: active reservation {} already exists for orderId: {}", existing.getId(), orderId);
                return ReservationDto.fromDomain(existing);
            }
        }

        // 2. Fetch authoritative Order aggregate
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot reserve inventory for an order with no items");
        }

        // 3. Extract & sort SKUs deterministically (SKU ASC) to prevent deadlocks
        List<String> sortedSkus = order.getItems().stream()
                .map(OrderItem::getSku)
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        // 4. Fetch inventory items with pessimistic write lock
        List<InventoryItem> lockedItems = inventoryRepository.findAllBySkuInOrderBySkuAscForUpdate(sortedSkus);
        Map<String, InventoryItem> inventoryMap = lockedItems.stream()
                .collect(Collectors.toMap(i -> i.getSku().trim().toUpperCase(), Function.identity()));

        // Ensure all requested SKUs exist in inventory
        for (String sku : sortedSkus) {
            if (!inventoryMap.containsKey(sku)) {
                log.warn("Inventory item missing for SKU: {}", sku);
                throw new InventoryItemNotFoundException(sku);
            }
        }

        // 5. ATOMIC VALIDATION: Check stock availability for all items before applying any changes
        for (OrderItem item : order.getItems()) {
            String itemSku = item.getSku().trim().toUpperCase();
            InventoryItem invItem = inventoryMap.get(itemSku);
            if (invItem.getAvailableQuantity() < item.getQuantity()) {
                log.warn("Insufficient stock for SKU {}: requested {}, available {}", itemSku, item.getQuantity(), invItem.getAvailableQuantity());
                throw new InsufficientStockException(itemSku, item.getQuantity(), invItem.getAvailableQuantity());
            }
        }

        // 6. APPLY RESERVATION & RECORD AUDIT MOVEMENTS
        UUID reservationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        List<StockReservationItem> reservationItems = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            String itemSku = item.getSku().trim().toUpperCase();
            InventoryItem invItem = inventoryMap.get(itemSku);
            int prevOnHand = invItem.getOnHandQuantity();
            int prevReserved = invItem.getReservedQuantity();

            invItem.reserve(item.getQuantity());
            inventoryRepository.save(invItem);

            stockMovementRepository.save(StockMovement.recordMovement(
                    invItem.getId(),
                    MovementType.RESERVATION,
                    item.getQuantity(),
                    "ORDER",
                    orderId.toString(),
                    prevOnHand,
                    invItem.getOnHandQuantity(),
                    prevReserved,
                    invItem.getReservedQuantity()
            ));

            reservationItems.add(new StockReservationItem(
                    UUID.randomUUID(),
                    reservationId,
                    invItem.getId(),
                    item.getProductId(),
                    item.getVariantId(),
                    itemSku,
                    item.getQuantity(),
                    now
            ));
        }

        // 7. Persist StockReservation
        String reservationRef = "RES-" + order.getOrderNumber();
        OffsetDateTime expiresAt = now.plus(inventoryProperties.getReservationTtl());

        StockReservation reservation = new StockReservation(
                reservationId,
                reservationRef,
                orderId,
                ReservationStatus.ACTIVE,
                expiresAt,
                null,
                reservationItems,
                now,
                now
        );

        StockReservation saved = reservationRepository.save(reservation);
        log.info("Successfully created stock reservation {} for order {}", saved.getReservationReference(), orderId);
        return ReservationDto.fromDomain(saved);
    }

    @Transactional
    public ReservationDto releaseReservation(UUID reservationId, String reason) {
        log.info("Releasing stock reservation: {} with reason: {}", reservationId, reason);

        StockReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (!reservation.isActive()) {
            log.info("Reservation {} is already in status {}, skipping release", reservationId, reservation.getStatus());
            return ReservationDto.fromDomain(reservation);
        }

        // Extract SKUs in deterministic order
        List<String> sortedSkus = reservation.getItems().stream()
                .map(StockReservationItem::getSku)
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        List<InventoryItem> lockedItems = inventoryRepository.findAllBySkuInOrderBySkuAscForUpdate(sortedSkus);
        Map<String, InventoryItem> inventoryMap = lockedItems.stream()
                .collect(Collectors.toMap(i -> i.getSku().trim().toUpperCase(), Function.identity()));

        for (StockReservationItem resItem : reservation.getItems()) {
            String itemSku = resItem.getSku().trim().toUpperCase();
            InventoryItem invItem = inventoryMap.get(itemSku);
            if (invItem != null) {
                int prevOnHand = invItem.getOnHandQuantity();
                int prevReserved = invItem.getReservedQuantity();

                invItem.release(resItem.getQuantity());
                inventoryRepository.save(invItem);

                stockMovementRepository.save(StockMovement.recordMovement(
                        invItem.getId(),
                        MovementType.RELEASE,
                        resItem.getQuantity(),
                        "RESERVATION_RELEASE",
                        reservationId.toString(),
                        prevOnHand,
                        invItem.getOnHandQuantity(),
                        prevReserved,
                        invItem.getReservedQuantity()
                ));
            }
        }

        reservation.release(reason);
        StockReservation saved = reservationRepository.save(reservation);
        log.info("Successfully released stock reservation {}", reservationId);
        return ReservationDto.fromDomain(saved);
    }

    @Transactional
    public int expireReservationsBatch() {
        OffsetDateTime now = OffsetDateTime.now();
        List<StockReservation> expiredActive = reservationRepository.findExpiredActiveReservations(
                now, PageRequest.of(0, inventoryProperties.getExpiryBatchSize())
        );

        if (expiredActive.isEmpty()) {
            return 0;
        }

        log.info("Found {} expired active reservations to process", expiredActive.size());
        int count = 0;
        for (StockReservation reservation : expiredActive) {
            try {
                releaseReservationInternal(reservation, "EXPIRED", MovementType.EXPIRY);
                count++;
            } catch (Exception e) {
                log.error("Failed to expire reservation {}: {}", reservation.getId(), e.getMessage(), e);
            }
        }
        return count;
    }

    private void releaseReservationInternal(StockReservation reservation, String reason, MovementType movementType) {
        List<String> sortedSkus = reservation.getItems().stream()
                .map(StockReservationItem::getSku)
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        if (!sortedSkus.isEmpty()) {
            List<InventoryItem> lockedItems = inventoryRepository.findAllBySkuInOrderBySkuAscForUpdate(sortedSkus);
            Map<String, InventoryItem> inventoryMap = lockedItems.stream()
                    .collect(Collectors.toMap(i -> i.getSku().trim().toUpperCase(), Function.identity()));

            for (StockReservationItem resItem : reservation.getItems()) {
                if (resItem.getSku() == null) continue;
                String itemSku = resItem.getSku().trim().toUpperCase();
                InventoryItem invItem = inventoryMap.get(itemSku);
                if (invItem != null && invItem.getReservedQuantity() > 0) {
                    int prevOnHand = invItem.getOnHandQuantity();
                    int prevReserved = invItem.getReservedQuantity();

                    int releaseQty = Math.min(resItem.getQuantity(), invItem.getReservedQuantity());
                    if (releaseQty > 0) {
                        invItem.release(releaseQty);
                        inventoryRepository.save(invItem);

                        stockMovementRepository.save(StockMovement.recordMovement(
                                invItem.getId(),
                                movementType,
                                releaseQty,
                                "SYSTEM_EXPIRY",
                                reservation.getId().toString(),
                                prevOnHand,
                                invItem.getOnHandQuantity(),
                                prevReserved,
                                invItem.getReservedQuantity()
                        ));
                    }
                }
            }
        }

        reservation.expire();
        reservationRepository.save(reservation);
    }

    @Transactional
    public InventoryItemDto adjustStock(StockAdjustmentCommand command) {
        String normalizedSku = command.sku() != null ? command.sku().trim().toUpperCase() : null;
        log.info("Admin stock adjustment for SKU {}: setting onHand to {}", normalizedSku, command.newOnHandQuantity());

        InventoryItem invItem = inventoryRepository.findBySkuForUpdate(normalizedSku)
                .orElseThrow(() -> new InventoryItemNotFoundException(normalizedSku));

        int prevOnHand = invItem.getOnHandQuantity();
        int prevReserved = invItem.getReservedQuantity();

        invItem.adjustOnHand(command.newOnHandQuantity());
        InventoryItem saved = inventoryRepository.save(invItem);

        stockMovementRepository.save(StockMovement.recordMovement(
                saved.getId(),
                MovementType.STOCK_ADJUSTMENT,
                command.newOnHandQuantity() - prevOnHand,
                "ADMIN_ADJUSTMENT",
                command.reason() != null ? command.reason() : "MANUAL_ADJUSTMENT",
                prevOnHand,
                saved.getOnHandQuantity(),
                prevReserved,
                saved.getReservedQuantity()
        ));

        return InventoryItemDto.fromDomain(saved);
    }

    @Transactional
    public InventoryItemDto createOrUpdateInitialStock(UUID productId, UUID variantId, String sku, int onHand) {
        String normalizedSku = sku != null ? sku.trim().toUpperCase() : null;
        Optional<InventoryItem> existingOpt = inventoryRepository.findBySku(normalizedSku);
        InventoryItem item;
        if (existingOpt.isPresent()) {
            item = existingOpt.get();
            item.adjustOnHand(onHand);
        } else {
            item = InventoryItem.createNew(productId, variantId, normalizedSku, onHand);
        }
        InventoryItem saved = inventoryRepository.save(item);
        return InventoryItemDto.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public InventoryItemDto getInventoryBySku(String sku) {
        String normalizedSku = sku != null ? sku.trim().toUpperCase() : null;
        return inventoryRepository.findBySku(normalizedSku)
                .map(InventoryItemDto::fromDomain)
                .orElseThrow(() -> new InventoryItemNotFoundException(normalizedSku));
    }

    @Transactional
    public ReservationDto commitReservation(UUID reservationId) {
        log.info("Committing stock reservation: {}", reservationId);

        StockReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (!reservation.isActive()) {
            log.info("Reservation {} is already in status {}, skipping commit", reservationId, reservation.getStatus());
            return ReservationDto.fromDomain(reservation);
        }

        List<String> sortedSkus = reservation.getItems().stream()
                .map(StockReservationItem::getSku)
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        List<InventoryItem> lockedItems = inventoryRepository.findAllBySkuInOrderBySkuAscForUpdate(sortedSkus);
        Map<String, InventoryItem> inventoryMap = lockedItems.stream()
                .collect(Collectors.toMap(i -> i.getSku().trim().toUpperCase(), Function.identity()));

        for (StockReservationItem resItem : reservation.getItems()) {
            String itemSku = resItem.getSku().trim().toUpperCase();
            InventoryItem invItem = inventoryMap.get(itemSku);
            if (invItem != null) {
                int prevOnHand = invItem.getOnHandQuantity();
                int prevReserved = invItem.getReservedQuantity();

                invItem.commit(resItem.getQuantity());
                inventoryRepository.save(invItem);

                stockMovementRepository.save(StockMovement.recordMovement(
                        invItem.getId(),
                        MovementType.COMMIT,
                        resItem.getQuantity(),
                        "FULFILLMENT_COMMIT",
                        reservationId.toString(),
                        prevOnHand,
                        invItem.getOnHandQuantity(),
                        prevReserved,
                        invItem.getReservedQuantity()
                ));
            }
        }

        reservation.commit();
        StockReservation saved = reservationRepository.save(reservation);
        log.info("Successfully committed stock reservation {}", reservationId);
        return ReservationDto.fromDomain(saved);
    }

    @Transactional
    public InventoryItemDto recordDamagedStock(String sku, int quantity, String reason) {
        String normalizedSku = sku != null ? sku.trim().toUpperCase() : null;
        log.info("Recording damaged stock for SKU {}: quantity {}", normalizedSku, quantity);

        InventoryItem invItem = inventoryRepository.findBySkuForUpdate(normalizedSku)
                .orElseThrow(() -> new InventoryItemNotFoundException(normalizedSku));

        int prevOnHand = invItem.getOnHandQuantity();
        int prevReserved = invItem.getReservedQuantity();

        invItem.recordDamaged(quantity);
        InventoryItem saved = inventoryRepository.save(invItem);

        stockMovementRepository.save(StockMovement.recordMovement(
                saved.getId(),
                MovementType.DAMAGE,
                quantity,
                "WAREHOUSE_QA",
                reason != null ? reason : "DAMAGED_STOCK",
                prevOnHand,
                saved.getOnHandQuantity(),
                prevReserved,
                saved.getReservedQuantity()
        ));

        return InventoryItemDto.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public List<StockMovementDto> listMovementsForSku(String sku) {
        String normalizedSku = sku != null ? sku.trim().toUpperCase() : null;
        InventoryItem item = inventoryRepository.findBySku(normalizedSku)
                .orElseThrow(() -> new InventoryItemNotFoundException(normalizedSku));

        return stockMovementRepository.findByInventoryItemId(item.getId()).stream()
                .map(StockMovementDto::fromDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockAvailabilityDto getInventoryAvailability(String sku) {
        String normalizedSku = sku != null ? sku.trim().toUpperCase() : null;
        InventoryItem item = inventoryRepository.findBySku(normalizedSku)
                .orElseThrow(() -> new InventoryItemNotFoundException(normalizedSku));

        return StockAvailabilityDto.fromDomain(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDto> listAllInventory() {
        return listAllInventory(org.springframework.data.domain.PageRequest.of(0, 50));
    }

    @Transactional(readOnly = true)
    public List<InventoryItemDto> listAllInventory(org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Pageable p = pageable != null ? pageable : org.springframework.data.domain.PageRequest.of(0, 50);
        return inventoryRepository.findAll().stream()
                .skip((long) p.getPageNumber() * p.getPageSize())
                .limit(p.getPageSize())
                .map(InventoryItemDto::fromDomain)
                .toList();
    }
}
