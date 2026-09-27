package org.example.md04_ss12.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.config.RedisCacheConfig;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Bai 4 - Product-Service (Subscriber): nhan product_id tu channel "promotion-updates"
 * va xoa cache cua san pham do -> lan truy van ke tiep se doc gia moi tu DB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionUpdateSubscriber implements MessageListener {

    private final CacheManager cacheManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8).trim();
        try {
            Long productId = Long.valueOf(body);
            Cache cache = cacheManager.getCache(RedisCacheConfig.PRODUCT_CACHE);
            if (cache != null) {
                cache.evict(productId);
            }
            log.info("[product-service] Nhan promotion-updates cho product {} -> da xoa cache", productId);
        } catch (NumberFormatException e) {
            log.warn("[product-service] Bo qua message khong hop le tren promotion-updates: '{}'", body);
        }
    }
}
