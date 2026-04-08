package com.farmchainx.farmchainx_backend.repository;

import com.farmchainx.farmchainx_backend.model.OtpStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpRepository
        extends JpaRepository<OtpStore, Long> {

    @Query("SELECT o FROM OtpStore o WHERE o.contact = :contact " +
            "AND o.role = :role AND o.used = false " +
            "ORDER BY o.createdAt DESC")
    Optional<OtpStore> findLatestOtp(
            @Param("contact") String contact,
            @Param("role") String role);
}