package org.example.md04_ss12.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.event.ShippingStatus;
import org.example.md04_ss12.event.ShippingStatusEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bai 5 - Order-Service nhan ShippingStatusEvent:
 * trang thai DELIVERED -> cap nhat don hang thanh COMPLETED.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingStatusListener {

    private final OrderRepository orderRepository;

    @Transactional
    @KafkaListener(topics = "${app.kafka.topic.shipping-events}", groupId = "order-service")
    public void onShippingStatus(ShippingStatusEvent event) {
        if (event.status() != ShippingStatus.DELIVERED) {
            log.info("[order-service] Don {} dang o trang thai van chuyen {} -> khong doi trang thai don",
                    event.orderId(), event.status());
            return;
        }
        orderRepository.findById(event.orderId()).ifPresentOrElse(order -> {
            if (order.getStatus() == OrderStatus.COMPLETED) {
                log.info("[order-service] Don {} da COMPLETED truoc do -> bo qua event trung", order.getId());
                return;
            }
            order.setStatus(OrderStatus.COMPLETED);
            log.info("[order-service] Don {} da giao thanh cong -> COMPLETED", order.getId());
        }, () -> log.warn("[order-service] Khong tim thay don {} cho ShippingStatusEvent", event.orderId()));
    }
}
