package com.farmchainx.farmchainx_backend.repository;

import com.farmchainx.farmchainx_backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository
        extends JpaRepository<Order, Long> {

    List<Order> findByConsumerIdOrderByOrderedAtDesc(
            Long consumerId);

    List<Order> findByProductFarmerIdOrderByOrderedAtDesc(
            Long farmerId);

    Optional<Order> findByOrderId(String orderId);
}