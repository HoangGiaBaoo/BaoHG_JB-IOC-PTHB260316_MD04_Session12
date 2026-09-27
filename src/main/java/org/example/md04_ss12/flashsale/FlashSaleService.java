package org.example.md04_ss12.flashsale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.common.BusyException;
import org.example.md04_ss12.common.OutOfStockException;
import org.example.md04_ss12.common.ResourceNotFoundException;
import org.example.md04_ss12.event.FlashSaleOrderEvent;
import org.example.md04_ss12.order.OrderRepository;
import org.example.md04_ss12.order.OrderSource;
import org.example.md04_ss12.product.Product;
import org.example.md04_ss12.product.ProductRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bai 6 - He thong Flash Sale "bat tu".
 * 1. Bat dau chuong trinh: nap ton kho + gia flash sale vao Redis.
 * 2. Mua ngay: gianh khoa lock:flashsale:{id} -> kiem tra & tru ton kho NGAY TREN REDIS (khong cham DB).
 * 3. Tru thanh cong -> day FlashSaleOrderEvent vao Kafka; Order-Service luu DB o phia sau.
 * => Request cua khach khong bao gio cham vao PostgreSQL, DB chi nhan dung so don thanh cong
 *    voi toc do do consumer quyet dinh.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleService {

    private static final long LOCK_WAIT_MILLIS = 3000;

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Value("${app.kafka.topic.flash-sale-orders}")
    private String flashSaleOrdersTopic;

    static String stockKey(Long productId) {
        return "flashsale:" + productId + ":stock";
    }

    static String priceKey(Long productId) {
        return "flashsale:" + productId + ":price";
    }

    static String lockKey(Long productId) {
        return "lock:flashsale:" + productId;
    }

    /** Buoc 1: nap ton kho flash sale vao Redis ngay khi chuong trinh bat dau. */
    public FlashSaleStatus start(Long productId, int quantity, int discountPercent, Duration duration) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id = " + productId));
        BigDecimal salePrice = product.getPrice()
                .multiply(BigDecimal.valueOf(100 - discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        redisTemplate.opsForValue().set(priceKey(productId), salePrice.toPlainString(), duration);
        redisTemplate.opsForValue().set(stockKey(productId), String.valueOf(quantity), duration);
        log.info("[flash-sale] Bat dau flash sale product {}: {} suat, gia {}, thoi gian {}",
                productId, quantity, salePrice, duration);
        return status(productId);
    }

    /** Buoc 2 + 3: mua ngay. */
    public FlashSalePurchaseResponse buy(Long productId, FlashSaleBuyRequest request) {
        // Chan som khi da het hang: khong can tranh khoa -> giam tai cho Redis khi hang van request don vao
        String current = redisTemplate.opsForValue().get(stockKey(productId));
        if (current == null) {
            throw new IllegalStateException("Chương trình Flash Sale chưa bắt đầu hoặc đã kết thúc");
        }
        if (Long.parseLong(current) < request.quantity()) {
            throw new OutOfStockException();
        }

        long remaining = decreaseStockWithLock(productId, request.quantity());
        BigDecimal unitPrice = new BigDecimal(redisTemplate.opsForValue().get(priceKey(productId)));

        FlashSaleOrderEvent event = new FlashSaleOrderEvent(UUID.randomUUID().toString(),
                request.customerEmail(), productId, request.quantity(), unitPrice, Instant.now());

        kafkaTemplate.send(flashSaleOrdersTopic, productId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // Bu tru: gui Kafka that bai -> tra lai suat mua vao Redis
                        redisTemplate.opsForValue().increment(stockKey(productId), request.quantity());
                        log.error("[flash-sale] Gui event {} that bai -> hoan lai {} suat",
                                event.orderCode(), request.quantity(), ex);
                    }
                });

        return new FlashSalePurchaseResponse(event.orderCode(), "PROCESSING",
                "Đặt hàng thành công, đơn hàng đang được xử lý", remaining);
    }

    private long decreaseStockWithLock(Long productId, int quantity) {
        RLock lock = redissonClient.getLock(lockKey(productId));
        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_WAIT_MILLIS, TimeUnit.MILLISECONDS);
            if (!acquired) {
                // Het thoi gian cho khoa: neu hang da ban het trong luc cho thi bao het hang, khong bao "ban"
                String left = redisTemplate.opsForValue().get(stockKey(productId));
                if (left == null || Long.parseLong(left) < quantity) {
                    throw new OutOfStockException();
                }
                throw new BusyException("Quá nhiều người đang mua, vui lòng thử lại");
            }
            String value = redisTemplate.opsForValue().get(stockKey(productId));
            if (value == null) {
                throw new IllegalStateException("Chương trình Flash Sale chưa bắt đầu hoặc đã kết thúc");
            }
            if (Long.parseLong(value) < quantity) {
                throw new OutOfStockException();
            }
            return redisTemplate.opsForValue().decrement(stockKey(productId), quantity);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusyException("Yêu cầu bị gián đoạn, vui lòng thử lại");
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public FlashSaleStatus status(Long productId) {
        String stock = redisTemplate.opsForValue().get(stockKey(productId));
        String price = redisTemplate.opsForValue().get(priceKey(productId));
        Long ttl = redisTemplate.getExpire(stockKey(productId), TimeUnit.SECONDS);
        long savedOrders = orderRepository.countByProductIdAndSource(productId, OrderSource.FLASH_SALE);
        return new FlashSaleStatus(productId, stock != null,
                stock == null ? null : Long.valueOf(stock),
                price == null ? null : new BigDecimal(price),
                ttl, savedOrders);
    }

    /** Mo phong N khach cung bam "Mua ngay" 1 luc. */
    public SimulationResult simulate(Long productId, int buyers) {
        AtomicInteger success = new AtomicInteger();
        AtomicInteger outOfStock = new AtomicInteger();
        AtomicInteger busy = new AtomicInteger();
        CountDownLatch startGate = new CountDownLatch(1);
        long begin;

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < buyers; i++) {
                String email = "fs-buyer" + i + "@test.com";
                pool.submit(() -> {
                    startGate.await();
                    try {
                        buy(productId, new FlashSaleBuyRequest(email, 1));
                        success.incrementAndGet();
                    } catch (OutOfStockException e) {
                        outOfStock.incrementAndGet();
                    } catch (BusyException e) {
                        busy.incrementAndGet();
                    }
                    return null;
                });
            }
            begin = System.nanoTime();
            startGate.countDown();
        }
        long elapsedMs = (System.nanoTime() - begin) / 1_000_000;
        return new SimulationResult(buyers, success.get(), outOfStock.get(), busy.get(), elapsedMs, status(productId));
    }

    public record SimulationResult(int buyers, int success, int outOfStock, int busy,
                                   long elapsedMs, FlashSaleStatus status) {
    }
}
