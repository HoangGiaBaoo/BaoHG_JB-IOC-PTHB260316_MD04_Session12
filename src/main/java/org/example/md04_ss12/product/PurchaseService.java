package org.example.md04_ss12.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.common.OutOfStockException;
import org.example.md04_ss12.common.ResourceNotFoundException;
import org.example.md04_ss12.order.Order;
import org.example.md04_ss12.order.OrderRepository;
import org.example.md04_ss12.order.OrderSource;
import org.example.md04_ss12.order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bai 3 - Chong ban lo bang Redisson Lock.
 * Thu tu: gianh khoa -> mo transaction -> kiem tra ton kho -> tru kho + tao don -> COMMIT -> nha khoa.
 * Transaction phai commit TRUOC khi nha khoa, neu khong nguoi ke tiep co the doc ton kho cu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductLockService lockService;
    private final TransactionTemplate transactionTemplate;

    public Order purchase(Long productId, PurchaseRequest request) {
        return lockService.withLock(productId, () -> transactionTemplate.execute(status -> {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id = " + productId));
            if (product.getStock() < request.quantity()) {
                throw new OutOfStockException();
            }
            product.setStock(product.getStock() - request.quantity());

            Order order = new Order();
            order.setOrderCode(UUID.randomUUID().toString());
            order.setCustomerEmail(request.customerEmail());
            order.setProductId(productId);
            order.setQuantity(request.quantity());
            order.setUnitPrice(product.getPrice());
            order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(request.quantity())));
            order.setStatus(OrderStatus.CREATED);
            order.setSource(OrderSource.LOCKED);
            return orderRepository.save(order);
        }));
    }

    /**
     * Mo phong N nguoi cung bam mua 1 luc (dung de kiem chung ket qua Bai 3).
     * Tat ca thread cho o vach xuat phat (CountDownLatch) roi cung lao vao mot luc.
     */
    public SimulationResult simulate(Long productId, int buyers) throws InterruptedException {
        int stockBefore = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id = " + productId))
                .getStock();

        AtomicInteger success = new AtomicInteger();
        AtomicInteger outOfStock = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();
        CountDownLatch startGate = new CountDownLatch(1);

        try (ExecutorService pool = Executors.newFixedThreadPool(Math.min(buyers, 200))) {
            for (int i = 0; i < buyers; i++) {
                String email = "buyer" + i + "@test.com";
                pool.submit(() -> {
                    try {
                        startGate.await();
                        purchase(productId, new PurchaseRequest(email, 1));
                        success.incrementAndGet();
                    } catch (OutOfStockException e) {
                        outOfStock.incrementAndGet();
                    } catch (Exception e) {
                        log.warn("Mua that bai: {}", e.getMessage());
                        error.incrementAndGet();
                    }
                    return null;
                });
            }
            startGate.countDown();
        } // close() cho toan bo task chay xong

        int stockAfter = productRepository.findById(productId).orElseThrow().getStock();
        return new SimulationResult(buyers, stockBefore, success.get(), outOfStock.get(), error.get(), stockAfter);
    }

    public record SimulationResult(int buyers, int stockBefore, int success, int outOfStock,
                                   int error, int stockAfter) {
    }
}
