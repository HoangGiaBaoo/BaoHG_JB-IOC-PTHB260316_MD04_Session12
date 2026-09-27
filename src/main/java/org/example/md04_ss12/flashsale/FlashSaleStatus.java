package org.example.md04_ss12.flashsale;

import java.math.BigDecimal;

public record FlashSaleStatus(
        Long productId,
        boolean running,
        Long remainingStock,
        BigDecimal salePrice,
        Long secondsLeft,
        long ordersSavedInDb
) {
}
