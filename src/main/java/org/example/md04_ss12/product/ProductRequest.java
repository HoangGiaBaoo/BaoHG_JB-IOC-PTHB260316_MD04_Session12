package org.example.md04_ss12.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank String name,
        String description,
        String imageUrl,
        @NotNull @DecimalMin("0") BigDecimal price,
        @Min(0) int stock
) {
}
