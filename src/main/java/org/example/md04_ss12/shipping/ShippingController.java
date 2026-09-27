package org.example.md04_ss12.shipping;

import lombok.RequiredArgsConstructor;
import org.example.md04_ss12.event.ShippingStatus;
import org.example.md04_ss12.event.ShippingStatusEvent;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipping/orders/{orderId}")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    /** Shipper bam "Da giao hang". */
    @PostMapping("/delivered")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ShippingStatusEvent delivered(@PathVariable Long orderId,
                                         @RequestParam(defaultValue = "Shipper") String shipperName) {
        return shippingService.updateStatus(orderId, ShippingStatus.DELIVERED, shipperName);
    }

    /** Cap nhat trang thai van chuyen bat ky (PICKED_UP, IN_TRANSIT, DELIVERED, FAILED). */
    @PostMapping("/status")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ShippingStatusEvent updateStatus(@PathVariable Long orderId,
                                            @RequestParam ShippingStatus status,
                                            @RequestParam(defaultValue = "Shipper") String shipperName) {
        return shippingService.updateStatus(orderId, status, shipperName);
    }
}
