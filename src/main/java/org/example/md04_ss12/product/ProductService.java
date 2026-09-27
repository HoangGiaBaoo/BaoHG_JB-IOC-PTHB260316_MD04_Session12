package org.example.md04_ss12.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.md04_ss12.common.ResourceNotFoundException;
import org.example.md04_ss12.config.RedisCacheConfig;
import org.example.md04_ss12.promotion.Promotion;
import org.example.md04_ss12.promotion.PromotionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;

    /**
     * Bai 1 - Cache-aside: lan dau doc DB roi ghi vao Redis (key = products::{id}, TTL 30 phut),
     * cac lan sau tra thang tu Redis, khong cham vao DB.
     */
    @Cacheable(cacheNames = RedisCacheConfig.PRODUCT_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        log.info("[product-service] CACHE MISS -> query DB cho product {}", id);
        Product product = findEntity(id);
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public int getStock(Long id) {
        return findEntity(id).getStock();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    /** Bai 1: quan tri vien cap nhat san pham -> xoa cache de lan doc sau lay du lieu moi. */
    @CacheEvict(cacheNames = RedisCacheConfig.PRODUCT_CACHE, key = "#id")
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntity(id);
        apply(product, request);
        log.info("[product-service] Cap nhat product {} -> evict cache", id);
        return toResponse(product);
    }

    @CacheEvict(cacheNames = RedisCacheConfig.PRODUCT_CACHE, key = "#id")
    @Transactional
    public void delete(Long id) {
        productRepository.delete(findEntity(id));
    }

    private Product findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id = " + id));
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setImageUrl(request.imageUrl());
        product.setPrice(request.price());
        product.setStock(request.stock());
    }

    private ProductResponse toResponse(Product product) {
        Optional<Promotion> promotion = promotionRepository.findActiveByProductId(product.getId(), Instant.now());
        Integer discount = promotion.map(Promotion::getDiscountPercent).orElse(null);
        BigDecimal finalPrice = product.getPrice();
        if (discount != null) {
            finalPrice = product.getPrice()
                    .multiply(BigDecimal.valueOf(100 - discount))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                product.getImageUrl(), product.getPrice(), discount, finalPrice, Instant.now());
    }
}
