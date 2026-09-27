package org.example.md04_ss12.promotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByProductIdAndActiveTrue(Long productId);

    default Optional<Promotion> findActiveByProductId(Long productId, Instant now) {
        return findRunning(productId, now).stream().findFirst();
    }

    @Query("""
            select p from Promotion p
            where p.productId = :productId and p.active = true
              and p.startAt <= :now and p.endAt > :now
            order by p.discountPercent desc
            """)
    List<Promotion> findRunning(@Param("productId") Long productId, @Param("now") Instant now);
}
