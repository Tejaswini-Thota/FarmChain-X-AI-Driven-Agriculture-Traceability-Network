package com.farmchainx.farmchainx_backend.repository;

import com.farmchainx.farmchainx_backend.model.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConsumerRepository
        extends JpaRepository<Consumer, Long> {

    Optional<Consumer> findByEmail(String email);
    Optional<Consumer> findByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
}