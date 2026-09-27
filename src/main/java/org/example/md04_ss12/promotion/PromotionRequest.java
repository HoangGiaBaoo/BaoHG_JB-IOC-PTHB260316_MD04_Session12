package org.example.md04_ss12.promotion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** startAt/endAt de trong -> khuyen mai bat dau ngay va keo dai 7 ngay. */
public record PromotionRequest(
        @NotNull Long productId,
        @Min(1) @Max(99) int discountPercent,
        Instant startAt,
        Instant endAt
) {
}
