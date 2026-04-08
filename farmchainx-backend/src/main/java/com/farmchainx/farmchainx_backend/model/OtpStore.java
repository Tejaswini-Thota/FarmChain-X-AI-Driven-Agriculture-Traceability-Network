package com.farmchainx.farmchainx_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contact;

    @Column(nullable = false, length = 6)
    private String otpCode;

    @Column(nullable = false)
    private String role;

    @Builder.Default
    private Integer attempts = 0;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    private Boolean used = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid(String code) {
        return !used && !isExpired()
                && otpCode.equals(code)
                && attempts < 5;
    }
}