package org.example.md04_ss12.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.event.OrderCreatedEvent;
import org.example.md04_ss12.product.ProductLockService;
import org.example.md04_ss12.product.ProductRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Bai 2 - Inventory-Service: group-id rieng "inventory-service", tru kho cho moi don moi.
 * Dung chung khoa lock:product:{id} voi Bai 3 de khong xung dot khi cung tru kho.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryListener {

    private final ProductRepository productRepository;
    private final ProductLockService lockService;

    @KafkaListener(topics = "${app.kafka.topic.order-events}", groupId = "inventory-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        int updated = lockService.withLock(event.productId(),
                () -> productRepository.decreaseStock(event.productId(), event.quantity()));
        if (updated == 1) {
            log.info("[inventory-service] Da tru {} san pham {} cho don #{}",
                    event.quantity(), event.productId(), event.orderId());
        } else {
            log.warn("[inventory-service] Khong du ton kho san pham {} cho don #{} (can {})",
                    event.productId(), event.orderId(), event.quantity());
        }
    }
}
