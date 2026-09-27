package org.example.md04_ss12.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.common.ResourceNotFoundException;
import org.example.md04_ss12.event.OrderCreatedEvent;
import org.example.md04_ss12.product.Product;
import org.example.md04_ss12.product.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Bai 2 - Order-Service (Producer).
 * Luu don vao DB roi gui OrderCreatedEvent vao topic "order-events" va tra ve ngay,
 * cac viec tru kho / gui mail do consumer xu ly bat dong bo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.order-events}")
    private String orderEventsTopic;

    public Order createOrder(CreateOrderRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id = " + request.productId()));

        Order order = new Order();
        order.setOrderCode(UUID.randomUUID().toString());
        order.setCustomerEmail(request.customerEmail());
        order.setProductId(product.getId());
        order.setQuantity(request.quantity());
        order.setUnitPrice(product.getPrice());
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(request.quantity())));
        order.setStatus(OrderStatus.CREATED);
        order.setSource(OrderSource.ASYNC);
        // save() tu commit transaction rieng -> event chi duoc gui khi don da nam trong DB
        Order saved = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(saved.getId(), saved.getOrderCode(),
                saved.getCustomerEmail(), saved.getProductId(), saved.getQuantity(),
                saved.getTotalAmount(), saved.getCreatedAt());

        // Khong cho ket qua gui: request ket thuc ngay khi message duoc day vao Kafka
        kafkaTemplate.send(orderEventsTopic, saved.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[order-service] Gui OrderCreatedEvent cho don {} that bai", saved.getId(), ex);
                    } else {
                        log.info("[order-service] Da gui OrderCreatedEvent don {} -> partition {}, offset {}",
                                saved.getId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
        return saved;
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng id = " + id));
    }

    @Transactional(readOnly = true)
    public List<Order> getAll() {
        return orderRepository.findAll();
    }
}
