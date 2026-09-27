package org.example.md04_ss12.order;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotBlank @Email String customerEmail,
        @NotNull Long productId,
        @Min(1) int quantity
) {
}
