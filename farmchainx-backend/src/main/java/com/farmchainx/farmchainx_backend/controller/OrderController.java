package com.farmchainx.farmchainx_backend.controller;

import com.farmchainx.farmchainx_backend.model.*;
import com.farmchainx.farmchainx_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final ConsumerRepository consumerRepo;

    // ── PLACE ORDER ──
    @PostMapping
    public ResponseEntity<Map<String,Object>> placeOrder(
            @RequestBody Map<String,Object> req) {

        Long consumerId = Long.valueOf(
                req.get("consumerId").toString());
        Long productId = Long.valueOf(
                req.get("productId").toString());
        Double qty = Double.valueOf(
                req.get("quantityKg").toString());

        Consumer consumer = consumerRepo.findById(consumerId)
                .orElseThrow(() ->
                        new RuntimeException("Consumer not found"));
        Product product = productRepo.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        double total = product.getPricePerKg() * qty;
        double saved = (product.getMarketPrice() != null
                ? product.getMarketPrice()
                : product.getPricePerKg()) * qty - total;

        Order order = Order.builder()
                .consumer(consumer)
                .product(product)
                .quantityKg(qty)
                .totalAmount(total)
                .savedAmount(Math.max(0, saved))
                .pricePerKg(product.getPricePerKg())
                .deliveryAddress((String) req.get("deliveryAddress"))
                .deliveryCity((String) req.get("deliveryCity"))
                .deliveryState((String) req.get("deliveryState"))
                .deliveryPincode((String) req.get("deliveryPincode"))
                .paymentMethod("COD".equals(req.get("paymentMethod"))
                        ? Order.PaymentMethod.COD
                        : Order.PaymentMethod.ONLINE)
                .paymentStatus("COD".equals(req.get("paymentMethod"))
                        ? Order.PaymentStatus.PENDING
                        : Order.PaymentStatus.PAID)
                .build();

        // Update product stock
        product.setSoldKg(product.getSoldKg() + qty);
        productRepo.save(product);

        Order saved2 = orderRepo.save(order);
        return ResponseEntity.ok(toMap(saved2));
    }

    // ── GET CONSUMER ORDERS ──
    @GetMapping("/consumer/{consumerId}")
    public ResponseEntity<List<Map<String,Object>>> getConsumerOrders(
            @PathVariable Long consumerId) {

        return ResponseEntity.ok(
                orderRepo.findByConsumerIdOrderByOrderedAtDesc(consumerId)
                        .stream().map(this::toMap)
                        .collect(Collectors.toList()));
    }

    // ── GET FARMER ORDERS ──
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<Map<String,Object>>> getFarmerOrders(
            @PathVariable Long farmerId) {

        return ResponseEntity.ok(
                orderRepo.findByProductFarmerIdOrderByOrderedAtDesc(
                                farmerId)
                        .stream().map(this::toMap)
                        .collect(Collectors.toList()));
    }

    // ── UPDATE ORDER STATUS ──
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Map<String,Object>> updateStatus(
            @PathVariable String orderId,
            @RequestBody Map<String,String> req) {

        Order order = orderRepo.findByOrderId(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        String status = req.get("status");
        order.setStatus(Order.OrderStatus.valueOf(status));
        return ResponseEntity.ok(toMap(orderRepo.save(order)));
    }

    private Map<String,Object> toMap(Order o) {
        Map<String,Object> m = new HashMap<>();
        m.put("id", o.getId());
        m.put("orderId", o.getOrderId());
        m.put("quantityKg", o.getQuantityKg());
        m.put("totalAmount", o.getTotalAmount());
        m.put("savedAmount", o.getSavedAmount());
        m.put("pricePerKg", o.getPricePerKg());
        m.put("deliveryAddress", o.getDeliveryAddress());
        m.put("deliveryCity", o.getDeliveryCity());
        m.put("deliveryPincode", o.getDeliveryPincode());
        m.put("paymentMethod", o.getPaymentMethod() != null
                ? o.getPaymentMethod().name() : null);
        m.put("paymentStatus", o.getPaymentStatus() != null
                ? o.getPaymentStatus().name() : null);
        m.put("status", o.getStatus() != null
                ? o.getStatus().name() : null);
        m.put("orderedAt", o.getOrderedAt());
        if (o.getProduct() != null) {
            m.put("productName", o.getProduct().getName());
            m.put("productEmoji", o.getProduct().getEmoji());
            if (o.getProduct().getFarmer() != null) {
                m.put("farmerName",
                        o.getProduct().getFarmer().getName());
            }
        }
        if (o.getConsumer() != null) {
            m.put("consumerName", o.getConsumer().getName());
        }
        return m;
    }
}
