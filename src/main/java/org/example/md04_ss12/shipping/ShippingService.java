package org.example.md04_ss12.shipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.event.ShippingStatus;
import org.example.md04_ss12.event.ShippingStatusEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Bai 5 - Shipping-Service: chi phat su kien, khong goi API cua Order/Notification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.shipping-events}")
    private String shippingEventsTopic;

    public ShippingStatusEvent updateStatus(Long orderId, ShippingStatus status, String shipperName) {
        ShippingStatusEvent event = new ShippingStatusEvent(orderId, status, shipperName, Instant.now());
        // key = orderId -> cac event cua cung 1 don vao cung partition, giu dung thu tu
        kafkaTemplate.send(shippingEventsTopic, orderId.toString(), event);
        log.info("[shipping-service] Gui ShippingStatusEvent don {} -> {}", orderId, status);
        return event;
    }
}
