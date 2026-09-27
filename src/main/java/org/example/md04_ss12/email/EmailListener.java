package org.example.md04_ss12.email;

import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.event.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Bai 2 - Email-Service: group-id rieng "email-service" nen nhan DAY DU moi event
 * cua topic "order-events", doc lap voi Inventory-Service.
 */
@Slf4j
@Component
public class EmailListener {

    @KafkaListener(topics = "${app.kafka.topic.order-events}", groupId = "email-service")
    public void onOrderCreated(OrderCreatedEvent event) throws InterruptedException {
        // Gia lap gui mail mat thoi gian - khong anh huong toi thoi gian phan hoi cua API dat hang
        Thread.sleep(1500);
        log.info("[email-service] Da gui email xac nhan don #{} ({}) toi {} - tong tien {}",
                event.orderId(), event.orderCode(), event.customerEmail(), event.totalAmount());
    }
}
