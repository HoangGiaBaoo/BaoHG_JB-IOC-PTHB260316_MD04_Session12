package org.example.md04_ss12.event;

import java.math.BigDecimal;
import java.time.Instant;

/** Bai 2: Order-Service gui vao topic "order-events" sau khi luu don hang. */
public record OrderCreatedEvent(
        Long orderId,
        String orderCode,
        String customerEmail,
        Long productId,
        int quantity,
        BigDecimal totalAmount,
        Instant createdAt
) {
}
