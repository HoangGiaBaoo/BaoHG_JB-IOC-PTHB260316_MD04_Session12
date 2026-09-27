package org.example.md04_ss12.flashsale;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/flash-sales/{productId}")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    @PostMapping("/start")
    public FlashSaleStatus start(@PathVariable Long productId,
                                 @RequestParam int quantity,
                                 @RequestParam(defaultValue = "50") int discountPercent,
                                 @RequestParam(defaultValue = "120") int durationMinutes) {
        return flashSaleService.start(productId, quantity, discountPercent, Duration.ofMinutes(durationMinutes));
    }

    /** "Mua ngay": 202 Accepted - don se duoc luu DB bat dong bo. */
    @PostMapping("/buy")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public FlashSalePurchaseResponse buy(@PathVariable Long productId,
                                         @Valid @RequestBody FlashSaleBuyRequest request) {
        return flashSaleService.buy(productId, request);
    }

    @GetMapping
    public FlashSaleStatus status(@PathVariable Long productId) {
        return flashSaleService.status(productId);
    }

    @PostMapping("/simulate")
    public FlashSaleService.SimulationResult simulate(@PathVariable Long productId,
                                                      @RequestParam(defaultValue = "1000") int buyers) {
        return flashSaleService.simulate(productId, buyers);
    }
}
