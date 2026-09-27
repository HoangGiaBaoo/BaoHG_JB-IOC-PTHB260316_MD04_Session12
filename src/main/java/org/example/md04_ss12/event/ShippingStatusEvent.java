package org.example.md04_ss12.event;

import java.time.Instant;

/** Bai 5: Shipping-Service gui vao topic "shipping-events". */
public record ShippingStatusEvent(
        Long orderId,
        ShippingStatus status,
        String shipperName,
        Instant occurredAt
) {
}
