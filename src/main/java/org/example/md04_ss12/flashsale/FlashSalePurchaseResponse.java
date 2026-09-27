package org.example.md04_ss12.flashsale;

public record FlashSalePurchaseResponse(
        String orderCode,
        String status,
        String message,
        long remainingStock
) {
}
