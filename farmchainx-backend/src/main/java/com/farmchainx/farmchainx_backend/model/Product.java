package com.farmchainx.farmchainx_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String emoji;
    private String batchName;
    private String category;

    private Double pricePerKg;
    private Double marketPrice;
    private Double mandiPrice;

    private Double quantityKg;

    @Builder.Default
    private Double soldKg = 0.0;

    private LocalDate harvestDate;
    private String certification;

    @Column(length = 500)
    private String certPhotoPath;

    @Builder.Default
    private Boolean certVerified = false;

    @Builder.Default
    private Integer aiTrustScore = 90;

    @Column(length = 500)
    private String videoPath;

    private String qrCode;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean outOfStock = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (soldKg == null) soldKg = 0.0;
        if (qrCode == null) {
            qrCode = "BCX-" +
                    name.substring(0, Math.min(2, name.length())).toUpperCase()
                    + "-0x" + Long.toHexString(System.currentTimeMillis())
                    .toUpperCase().substring(0, 6);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (quantityKg != null && soldKg != null) {
            outOfStock = (quantityKg - soldKg) <= 0;
        }
    }

    @Transient
    public Double getAvailableKg() {
        return quantityKg - soldKg;
    }
}