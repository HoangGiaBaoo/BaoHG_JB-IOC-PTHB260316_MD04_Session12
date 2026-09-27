package org.example.md04_ss12.product;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO duoc luu trong Redis cache (Bai 1).
 * Khong chua ton kho vi ton kho thay doi lien tuc; chi cache thong tin it thay doi + gia.
 * loadedAt cho biet lan cuoi du lieu duoc doc tu DB (de kiem chung cache hit/miss).
 */
public record ProductResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        BigDecimal price,
        Integer discountPercent,
        BigDecimal finalPrice,
        Instant loadedAt
) {
}
