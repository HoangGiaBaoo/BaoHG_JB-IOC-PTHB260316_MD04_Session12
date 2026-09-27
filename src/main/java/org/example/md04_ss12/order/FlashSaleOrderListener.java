package org.example.md04_ss12.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.event.FlashSaleOrderEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Bai 6 - Order-Service tieu thu FlashSaleOrderEvent va luu don vao PostgreSQL "thong tha" o phia sau.
 * Toc do ghi DB do consumer quyet dinh (so partition / concurrency), khong phu thuoc luong request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlashSaleOrderListener {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "${app.kafka.topic.flash-sale-orders}", groupId = "order-service", concurrency = "3")
    public void onFlashSaleOrder(FlashSaleOrderEvent event) {
        if (orderRepository.existsByOrderCode(event.orderCode())) {
            log.info("[order-service] Don flash sale {} da ton tai -> bo qua", event.orderCode());
            return;
        }
        Order order = new Order();
        order.setOrderCode(event.orderCode());
        order.setCustomerEmail(event.customerEmail());
        order.setProductId(event.productId());
        order.setQuantity(event.quantity());
        order.setUnitPrice(event.unitPrice());
        order.setTotalAmount(event.unitPrice().multiply(BigDecimal.valueOf(event.quantity())));
        order.setStatus(OrderStatus.CREATED);
        order.setSource(OrderSource.FLASH_SALE);
        order.setCreatedAt(event.purchasedAt());
        try {
            orderRepository.save(order);
            log.info("[order-service] Da luu don flash sale {} ({} x product {})",
                    event.orderCode(), event.quantity(), event.productId());
        } catch (DataIntegrityViolationException e) {
            // Unique orderCode: event bi giao lai dong thoi -> don da duoc luu boi lan truoc
            log.info("[order-service] Don flash sale {} bi trung -> bo qua", event.orderCode());
        }
    }
}
