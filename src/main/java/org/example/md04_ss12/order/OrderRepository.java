package org.example.md04_ss12.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderCode(String orderCode);

    List<Order> findByProductIdAndSource(Long productId, OrderSource source);

    long countByProductIdAndSource(Long productId, OrderSource source);
}
