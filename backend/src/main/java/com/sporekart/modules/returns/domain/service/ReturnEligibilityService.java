package com.sporekart.modules.returns.domain.service;

import com.sporekart.modules.order.domain.Order;
import com.sporekart.modules.order.domain.OrderItem;
import com.sporekart.modules.order.domain.OrderStatus;
import com.sporekart.modules.returns.domain.Return;
import com.sporekart.modules.returns.domain.ReturnItem;
import com.sporekart.modules.returns.domain.ReturnPolicy;
import com.sporekart.modules.returns.domain.ReturnStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReturnEligibilityService {

    private final ReturnPolicy returnPolicy = ReturnPolicy.defaultPolicy();

    public record ItemEligibilityResult(
            UUID orderItemId,
            UUID productId,
            String sku,
            String productName,
            int orderedQuantity,
            int previouslyReturnedQuantity,
            int returnableQuantity,
            boolean isReturnable,
            String reasonCode
    ) {}

    public record OrderEligibilityResult(
            UUID orderId,
            String orderReference,
            boolean eligible,
            String ineligibilityReason,
            OffsetDateTime deliveryTimestamp,
            OffsetDateTime returnDeadline,
            List<ItemEligibilityResult> itemEligibilities
    ) {}

    public OrderEligibilityResult evaluateEligibility(Order order, OffsetDateTime deliveryTimestamp, List<Return> existingReturns) {
        if (order == null) {
            return new OrderEligibilityResult(null, null, false, "ORDER_NOT_FOUND", null, null, List.of());
        }

        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.SHIPPED) {
            return new OrderEligibilityResult(
                    order.getId(),
                    order.getOrderNumber(),
                    false,
                    "ORDER_NOT_DELIVERED",
                    null,
                    null,
                    List.of()
            );
        }

        OffsetDateTime effectiveDelivery = deliveryTimestamp != null ? deliveryTimestamp : order.getUpdatedAt();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime deadline = returnPolicy.calculateDeadline(effectiveDelivery);

        if (!returnPolicy.isWithinReturnWindow(effectiveDelivery, now)) {
            return new OrderEligibilityResult(
                    order.getId(),
                    order.getOrderNumber(),
                    false,
                    "RETURN_WINDOW_EXPIRED",
                    effectiveDelivery,
                    deadline,
                    List.of()
            );
        }

        // Aggregate previously requested/approved quantities per orderItemId across active returns
        Map<UUID, Integer> activeReturnedQuantities = existingReturns.stream()
                .filter(r -> r.getStatus() != ReturnStatus.REJECTED && r.getStatus() != ReturnStatus.CANCELLED && r.getStatus() != ReturnStatus.RETURN_REJECTED)
                .flatMap(r -> r.getItems().stream())
                .collect(Collectors.groupingBy(
                        ReturnItem::getOrderItemId,
                        Collectors.summingInt(i -> Math.max(i.getRequestedQuantity(), i.getApprovedQuantity()))
                ));

        List<ItemEligibilityResult> itemEligibilities = new ArrayList<>();
        boolean anyReturnable = false;

        for (OrderItem item : order.getItems()) {
            int alreadyReturned = activeReturnedQuantities.getOrDefault(item.getId(), 0);
            int returnable = Math.max(0, item.getQuantity() - alreadyReturned);
            boolean isItemEligible = returnable > 0;

            String itemReason = isItemEligible ? "ELIGIBLE" : "QUANTITY_ALREADY_RETURNED";
            if (isItemEligible) {
                anyReturnable = true;
            }

            itemEligibilities.add(new ItemEligibilityResult(
                    item.getId(),
                    item.getProductId(),
                    item.getSku(),
                    item.getProductNameSnapshot(),
                    item.getQuantity(),
                    alreadyReturned,
                    returnable,
                    isItemEligible,
                    itemReason
            ));
        }

        String overallReason = anyReturnable ? "ELIGIBLE" : "ALL_ITEMS_RETURNED";

        return new OrderEligibilityResult(
                order.getId(),
                order.getOrderNumber(),
                anyReturnable,
                overallReason,
                effectiveDelivery,
                deadline,
                itemEligibilities
        );
    }
}
