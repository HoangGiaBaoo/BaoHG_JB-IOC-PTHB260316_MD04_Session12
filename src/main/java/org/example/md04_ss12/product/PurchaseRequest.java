package org.example.md04_ss12.product;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PurchaseRequest(
        @NotBlank @Email String customerEmail,
        @Min(1) int quantity
) {
}
