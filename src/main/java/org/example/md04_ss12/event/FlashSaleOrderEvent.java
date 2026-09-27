package org.example.md04_ss12.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Bai 6: gui vao topic "flash-sale-orders" sau khi tru ton kho thanh cong tren Redis.
 * orderCode la khoa idempotent: consumer nhan trung lap se khong tao 2 don.
 */
public record FlashSaleOrderEvent(
        String orderCode,
        String customerEmail,
        Long productId,
        int quantity,
        BigDecimal unitPrice,
        Instant purchasedAt
) {
}
