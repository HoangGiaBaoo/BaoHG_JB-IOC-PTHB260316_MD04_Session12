package org.example.md04_ss12.flashsale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record FlashSaleBuyRequest(
        @NotBlank @Email String customerEmail,
        @Min(1) @Max(5) int quantity
) {
}
