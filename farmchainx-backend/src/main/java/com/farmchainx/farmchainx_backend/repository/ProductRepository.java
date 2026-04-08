package com.farmchainx.farmchainx_backend.repository;

import com.farmchainx.farmchainx_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrueOrderByAiTrustScoreDesc();

    List<Product> findByActiveTrueAndCategoryOrderByAiTrustScoreDesc(
            String category);

    List<Product> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);

    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND p.farmer.state = :state " +
            "ORDER BY p.aiTrustScore DESC")
    List<Product> findByFarmerState(@Param("state") String state);
}