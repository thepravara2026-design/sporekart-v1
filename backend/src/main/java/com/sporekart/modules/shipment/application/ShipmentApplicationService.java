package com.sporekart.modules.shipment.application;

import com.sporekart.modules.order.application.OrderApplicationService;
import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderActorType;
import com.sporekart.modules.order.domain.exception.OrderNotFoundException;
import com.sporekart.modules.order.infrastructure.persistence.OrderRepository;
import com.sporekart.modules.shipment.application.dto.ShipmentDto;
import com.sporekart.modules.shipment.application.dto.ShipmentTrackingResponseDto;
import com.sporekart.modules.shipment.domain.PackageDetails;
import com.sporekart.modules.shipment.domain.Shipment;
import com.sporekart.modules.shipment.domain.ShipmentItem;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.domain.ShipmentReferenceGeneratorPort;
import com.sporekart.modules.shipment.domain.ShipmentStatus;
import com.sporekart.modules.shipment.domain.ShippingAddressSnapshot;
import com.sporekart.modules.shipment.domain.event.ShipmentLifecycleEvent;
import com.sporekart.modules.shipment.infrastructure.persistence.ShipmentRepository;
import com.sporekart.modules.shipment.infrastructure.persistence.ShippingWebhookEventEntity;
import com.sporekart.modules.shipment.infrastructure.provider.ShippingProvider;
import com.sporekart.modules.shipment.infrastructure.provider.ShippingProviderRegistry;
import com.sporekart.modules.shipment.infrastructure.provider.dto.NormalizedWebhookEvent;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentBookingRequest;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentBookingResult;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentCancellationRequest;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentCancellationResult;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentTrackingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShipmentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentApplicationService.class);

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderApplicationService orderApplicationService;
    private final ShippingProviderRegistry providerRegistry;
    private final ShipmentReferenceGeneratorPort referenceGenerator;
    private final ApplicationEventPublisher eventPublisher;

    public ShipmentApplicationService(
            ShipmentRepository shipmentRepository,
            OrderRepository orderRepository,
            OrderApplicationService orderApplicationService,
            ShippingProviderRegistry providerRegistry,
            ShipmentReferenceGeneratorPort referenceGenerator,
            ApplicationEventPublisher eventPublisher
    ) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.orderApplicationService = orderApplicationService;
        this.providerRegistry = providerRegistry;
        this.referenceGenerator = referenceGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ShipmentDto createShipmentForOrder(UUID orderId) {
        Optional<Shipment> existing = shipmentRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            return ShipmentDto.fromDomain(existing.get());
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        String ref = referenceGenerator.generateReference();
        ShippingProvider provider = providerRegistry.getDefaultProvider();

        ShippingAddressSnapshot addr = new ShippingAddressSnapshot(
                order.getShippingAddress().getFullName(),
                order.getShippingAddress().getPhone(),
                order.getShippingAddress().getAddressLine1(),
                order.getShippingAddress().getAddressLine2(),
                order.getShippingAddress().getCity(),
                order.getShippingAddress().getState(),
                order.getShippingAddress().getPostalCode(),
                order.getShippingAddress().getCountry()
        );

        PackageDetails pkg = new PackageDetails(
                500, 100, 100, 100, order.getGrandTotal()
        );

        List<ShipmentItem> items = order.getItems().stream()
                .map(item -> new ShipmentItem(
                        UUID.randomUUID(),
                        item.getId(),
                        item.getProductId(),
                        item.getSku(),
                        item.getProductNameSnapshot(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        Shipment shipment = Shipment.create(
                ref,
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                provider.getProviderType(),
                pkg,
                addr,
                items
        );

        shipment.markReadyForBooking(OrderActorType.SYSTEM, "FULFILMENT_SERVICE");
        Shipment saved = shipmentRepository.save(shipment);

        log.info("Created shipment {} for order {}", saved.getShipmentReference(), order.getOrderNumber());

        // Book shipment asynchronously/synchronously
        return bookShipmentInternal(saved.getId());
    }

    @Transactional
    public ShipmentDto bookShipment(UUID shipmentId) {
        return bookShipmentInternal(shipmentId);
    }

    private ShipmentDto bookShipmentInternal(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));

        if (shipment.getStatus() == ShipmentStatus.BOOKED) {
            return ShipmentDto.fromDomain(shipment); // Idempotent
        }

        shipment.markBookingPending(OrderActorType.SYSTEM, "BOOKING_ENGINE");
        shipment = shipmentRepository.save(shipment);

        ShippingProvider provider = providerRegistry.getProvider(shipment.getProvider());

        List<ShipmentBookingRequest.BookingItem> bookingItems = shipment.getItems().stream()
                .map(i -> new ShipmentBookingRequest.BookingItem(i.getSku(), i.getProductNameSnapshot(), i.getQuantity()))
                .collect(Collectors.toList());

        ShipmentBookingRequest req = new ShipmentBookingRequest(
                shipment.getShipmentReference(),
                shipment.getOrderReference(),
                shipment.getShippingAddress(),
                shipment.getPackageDetails(),
                bookingItems
        );

        ShipmentBookingResult result = provider.createAndBookShipment(req);

        if (result.success()) {
            shipment.markBooked(
                    result.providerShipmentId(),
                    result.awb(),
                    result.trackingNumber(),
                    result.courierName(),
                    result.courierCode(),
                    result.estimatedDeliveryAt(),
                    OrderActorType.SYSTEM,
                    "SHIPPING_PROVIDER"
            );

            Shipment saved = shipmentRepository.save(shipment);
            log.info("Successfully booked shipment {} with AWB {}", saved.getShipmentReference(), saved.getAwb());

            // Transition Order to SHIPPED via Order Application Service
            try {
                orderApplicationService.markShipped(saved.getOrderId(), "SHIPPING_SYSTEM", saved.getAwb());
            } catch (Exception e) {
                log.warn("Order status transition to SHIPPED deferred or already updated for order {}", saved.getOrderId());
            }

            publishEvent(saved, "Shipment booked successfully");
            return ShipmentDto.fromDomain(saved);
        } else {
            log.error("Failed to book shipment {}: {}", shipment.getShipmentReference(), result.errorMessage());
            shipmentRepository.save(shipment);
            return ShipmentDto.fromDomain(shipment);
        }
    }

    @Transactional
    public boolean processWebhook(ShipmentProviderType providerType, String rawBody, Map<String, String> headers) {
        ShippingProvider provider = providerRegistry.getProvider(providerType);

        if (!provider.verifyWebhookSignature(rawBody, headers)) {
            log.warn("Invalid webhook signature received for provider {}", providerType);
            return false;
        }

        NormalizedWebhookEvent event = provider.parseWebhookEvent(rawBody);

        if (shipmentRepository.existsWebhookEvent(providerType, event.providerEventId())) {
            log.info("Webhook event {} already processed for provider {}, skipping duplicate", event.providerEventId(), providerType);
            return true;
        }

        Optional<Shipment> shipmentOpt = shipmentRepository.findByAwb(event.awb());
        if (shipmentOpt.isEmpty()) {
            shipmentOpt = shipmentRepository.findByProviderShipmentId(event.providerShipmentId());
        }
        if (shipmentOpt.isEmpty()) {
            shipmentOpt = shipmentRepository.findByOrderReference(event.orderReference());
        }

        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            ShipmentStatus prevStatus = shipment.getStatus();

            shipment.addTrackingEvent(
                    event.providerEventId(),
                    event.rawProviderStatus(),
                    event.normalizedStatus(),
                    event.description(),
                    event.location(),
                    event.occurredAt()
            );

            Shipment saved = shipmentRepository.save(shipment);

            // Sync with Order domain if lifecycle state progressed
            if (saved.getStatus() != prevStatus) {
                syncOrderStatus(saved);
                publishEvent(saved, event.description());
            }
        } else {
            log.warn("Webhook received for unknown shipment/order: AWB={}, Order={}", event.awb(), event.orderReference());
        }

        ShippingWebhookEventEntity webhookLog = new ShippingWebhookEventEntity(
                UUID.randomUUID(),
                providerType,
                event.providerEventId(),
                event.rawProviderStatus(),
                rawBody,
                Instant.now()
        );
        shipmentRepository.saveWebhookEvent(webhookLog);

        return true;
    }

    private void syncOrderStatus(Shipment shipment) {
        try {
            switch (shipment.getStatus()) {
                case IN_TRANSIT, PICKED_UP -> orderApplicationService.markShipped(shipment.getOrderId(), "SHIPPING_SYSTEM", shipment.getAwb());
                case OUT_FOR_DELIVERY -> orderApplicationService.markOutForDelivery(shipment.getOrderId(), "SHIPPING_SYSTEM");
                case DELIVERED -> orderApplicationService.markDelivered(shipment.getOrderId(), "SHIPPING_SYSTEM");
                default -> {}
            }
        } catch (Exception e) {
            log.warn("Order sync warning for shipment {}: {}", shipment.getShipmentReference(), e.getMessage());
        }
    }

    @Transactional
    public ShipmentDto cancelShipment(String shipmentReference, String reason, OrderActorType actorType, String actorId) {
        Shipment shipment = shipmentRepository.findByShipmentReference(shipmentReference)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentReference));

        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            return ShipmentDto.fromDomain(shipment);
        }

        ShippingProvider provider = providerRegistry.getProvider(shipment.getProvider());
        if (shipment.getAwb() != null) {
            ShipmentCancellationRequest req = new ShipmentCancellationRequest(
                    shipment.getShipmentReference(),
                    shipment.getProviderShipmentId(),
                    shipment.getAwb(),
                    reason
            );
            ShipmentCancellationResult res = provider.cancelShipment(req);
            log.info("Provider cancel response for {}: {}", shipmentReference, res.message());
        }

        shipment.markCancelled(reason, actorType, actorId);
        Shipment saved = shipmentRepository.save(shipment);
        publishEvent(saved, reason);
        return ShipmentDto.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentDto getShipmentForCustomer(String orderReference, String customerId) {
        Shipment shipment = shipmentRepository.findByOrderReference(orderReference)
                .orElseThrow(() -> new IllegalArgumentException("No shipment found for order: " + orderReference));

        if (!shipment.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Access denied: Order ownership mismatch");
        }

        return ShipmentDto.fromDomain(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentTrackingResponseDto getTrackingForCustomer(String orderReference, String customerId) {
        ShipmentDto dto = getShipmentForCustomer(orderReference, customerId);
        return new ShipmentTrackingResponseDto(
                dto.shipmentReference(),
                dto.orderReference(),
                dto.status(),
                dto.awb(),
                dto.courierName(),
                dto.estimatedDeliveryAt(),
                dto.trackingEvents()
        );
    }

    @Transactional(readOnly = true)
    public Page<ShipmentDto> getAdminShipments(ShipmentStatus statusFilter, Pageable pageable) {
        return shipmentRepository.findAll(statusFilter, pageable).map(ShipmentDto::fromDomain);
    }

    @Transactional(readOnly = true)
    public ShipmentDto getAdminShipmentByReference(String shipmentReference) {
        return shipmentRepository.findByShipmentReference(shipmentReference)
                .map(ShipmentDto::fromDomain)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentReference));
    }

    @Transactional
    public ShipmentDto syncShipmentWithProvider(String shipmentReference) {
        Shipment shipment = shipmentRepository.findByShipmentReference(shipmentReference)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentReference));

        ShippingProvider provider = providerRegistry.getProvider(shipment.getProvider());
        ShipmentTrackingResult tracking = provider.getTrackingInfo(shipment.getProviderShipmentId(), shipment.getAwb());

        if (tracking != null && tracking.success()) {
            for (ShipmentTrackingResult.TrackingCheckpoint cp : tracking.checkpoints()) {
                shipment.addTrackingEvent(
                        cp.eventId(),
                        cp.providerStatus(),
                        cp.normalizedStatus(),
                        cp.description(),
                        cp.location(),
                        cp.timestamp()
                );
            }
            Shipment saved = shipmentRepository.save(shipment);
            syncOrderStatus(saved);
            return ShipmentDto.fromDomain(saved);
        }

        return ShipmentDto.fromDomain(shipment);
    }

    @Transactional(readOnly = true)
    public String getShipmentLabelUrl(String shipmentReference) {
        Shipment shipment = shipmentRepository.findByShipmentReference(shipmentReference)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentReference));
        ShippingProvider provider = providerRegistry.getProvider(shipment.getProvider());
        return provider.getLabelUrl(shipment.getProviderShipmentId(), shipment.getAwb());
    }

    @Transactional(readOnly = true)
    public String getShipmentManifestUrl(String shipmentReference) {
        Shipment shipment = shipmentRepository.findByShipmentReference(shipmentReference)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentReference));
        ShippingProvider provider = providerRegistry.getProvider(shipment.getProvider());
        return provider.getManifestUrl(shipment.getProviderShipmentId(), shipment.getAwb());
    }

    @Transactional
    public void reconcileActiveShipments() {
        List<Shipment> active = shipmentRepository.findActiveShipmentsForReconciliation();
        log.info("Starting reconciliation for {} active shipments", active.size());

        for (Shipment shipment : active) {
            try {
                ShippingProvider provider = providerRegistry.getProvider(shipment.getProvider());
                ShipmentTrackingResult tracking = provider.getTrackingInfo(shipment.getProviderShipmentId(), shipment.getAwb());
                if (tracking != null && tracking.success()) {
                    for (ShipmentTrackingResult.TrackingCheckpoint cp : tracking.checkpoints()) {
                        shipment.addTrackingEvent(
                                cp.eventId(),
                                cp.providerStatus(),
                                cp.normalizedStatus(),
                                cp.description(),
                                cp.location(),
                                cp.timestamp()
                        );
                    }
                    Shipment saved = shipmentRepository.save(shipment);
                    syncOrderStatus(saved);
                }
            } catch (Exception e) {
                log.error("Failed to reconcile shipment {}: {}", shipment.getShipmentReference(), e.getMessage());
            }
        }
    }

    private void publishEvent(Shipment shipment, String reason) {
        ShipmentStatus prev = shipment.getStatusHistories().size() > 1
                ? shipment.getStatusHistories().get(shipment.getStatusHistories().size() - 2).getNewStatus()
                : null;

        eventPublisher.publishEvent(ShipmentLifecycleEvent.of(
                shipment.getId(),
                shipment.getShipmentReference(),
                shipment.getOrderId(),
                shipment.getOrderReference(),
                shipment.getCustomerId(),
                prev,
                shipment.getStatus(),
                shipment.getAwb(),
                shipment.getCourierName(),
                reason
        ));
    }
}
