package com.farmchainx.farmchainx_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String state;
    private String district;
    private String village;
    private String pincode;
    private Double acresOfLand;

    @Column(length = 500)
    private String languages;

    @Column(length = 2000)
    private String profilePhotoPath;

    @Column(columnDefinition = "TEXT")
    private String farmImagePaths;

    @Column(length = 100)
    private String blockchainId;

    @Builder.Default
    private Boolean verified = false;

    @Builder.Default
    private String role = "FARMER";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (blockchainId == null) {
            blockchainId = "BCX-FARM-0x" +
                    Long.toHexString(System.currentTimeMillis())
                            .toUpperCase().substring(0, 8);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}