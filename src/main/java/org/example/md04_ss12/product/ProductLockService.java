package org.example.md04_ss12.product;

import lombok.RequiredArgsConstructor;
import org.example.md04_ss12.common.BusyException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Bai 3: khoa phan tan Redisson voi key "lock:product:{id}".
 * Moi thao tac doc - kiem tra - tru ton kho cua 1 san pham deu phai chay trong khoa nay
 * (ca Bai 3 lan Inventory consumer cua Bai 2) de khong bi lost update.
 */
@Component
@RequiredArgsConstructor
public class ProductLockService {

    private static final long WAIT_SECONDS = 10;

    private final RedissonClient redissonClient;

    public static String lockKey(Long productId) {
        return "lock:product:" + productId;
    }

    public <T> T withLock(Long productId, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey(productId));
        boolean acquired = false;
        try {
            // Khong truyen leaseTime -> Redisson watchdog tu gia han khoa khi xu ly con dang chay,
            // va tu nha khoa (sau 30s) neu instance bi chet giua chung.
            acquired = lock.tryLock(WAIT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusyException("Hệ thống đang bận, vui lòng thử lại");
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusyException("Yêu cầu bị gián đoạn, vui lòng thử lại");
        } finally {
            // Luon giai phong khoa trong finally de khong treo he thong
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
