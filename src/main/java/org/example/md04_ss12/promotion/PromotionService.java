package org.example.md04_ss12.promotion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.common.ResourceNotFoundException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Bai 4 - Promotion-Service (Publisher): moi khi khuyen mai thay doi,
 * gui product_id vao channel "promotion-updates" de moi service dang cache gia lam moi du lieu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final StringRedisTemplate redisTemplate;
    private final ChannelTopic promotionUpdatesTopic;

    @Transactional(readOnly = true)
    public List<Promotion> getAll() {
        return promotionRepository.findAll();
    }

    /** Tao khuyen mai moi (tat cac khuyen mai dang chay cua san pham do). */
    @Transactional
    public Promotion create(PromotionRequest request) {
        promotionRepository.findByProductIdAndActiveTrue(request.productId())
                .forEach(p -> p.setActive(false));

        Promotion promotion = new Promotion();
        promotion.setProductId(request.productId());
        promotion.setDiscountPercent(request.discountPercent());
        Instant start = request.startAt() != null ? request.startAt() : Instant.now();
        promotion.setStartAt(start);
        promotion.setEndAt(request.endAt() != null ? request.endAt() : start.plus(Duration.ofDays(7)));
        promotion = promotionRepository.save(promotion);

        publishAfterCommit(promotion.getProductId());
        return promotion;
    }

    @Transactional
    public void deactivate(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khuyến mãi id = " + promotionId));
        promotion.setActive(false);
        publishAfterCommit(promotion.getProductId());
    }

    /**
     * Chi publish sau khi transaction commit, tranh truong hop subscriber xoa cache
     * roi doc lai DB khi du lieu moi chua duoc commit (se cache lai gia cu).
     */
    private void publishAfterCommit(Long productId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisTemplate.convertAndSend(promotionUpdatesTopic.getTopic(), productId.toString());
                log.info("[promotion-service] Publish product {} vao channel {}", productId, promotionUpdatesTopic.getTopic());
            }
        });
    }
}
