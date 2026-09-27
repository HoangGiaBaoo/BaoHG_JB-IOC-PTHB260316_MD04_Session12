package org.example.md04_ss12.notification;

import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.event.ShippingStatus;
import org.example.md04_ss12.event.ShippingStatusEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Bai 5 - Notification-Service: cung nghe "shipping-events" (group-id rieng)
 * de gui tin chuc mung khi don duoc giao thanh cong.
 */
@Slf4j
@Component
public class NotificationListener {

    @KafkaListener(topics = "${app.kafka.topic.shipping-events}", groupId = "notification-service")
    public void onShippingStatus(ShippingStatusEvent event) {
        if (event.status() == ShippingStatus.DELIVERED) {
            log.info("[notification-service] Chuc mung! Don hang #{} da duoc shipper {} giao thanh cong. "
                    + "Cam on ban da mua sam!", event.orderId(), event.shipperName());
        } else {
            log.info("[notification-service] Don hang #{} cap nhat van chuyen: {}", event.orderId(), event.status());
        }
    }
}
