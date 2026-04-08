package com.farmchainx.farmchainx_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    private Consumer consumer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Double quantityKg;
    private Double totalAmount;
    private Double savedAmount;
    private Double pricePerKg;

    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryPincode;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PACKED;

    private LocalDateTime orderedAt;
    private LocalDateTime updatedAt;

    public enum PaymentMethod { ONLINE, COD }
    public enum PaymentStatus { PENDING, PAID, FAILED }
    public enum OrderStatus { PACKED, TRANSIT, DELIVERED, CANCELLED }

    @PrePersist
    protected void onCreate() {
        orderedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderId == null) {
            orderId = "ORD-" + System.currentTimeMillis() % 100000;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}