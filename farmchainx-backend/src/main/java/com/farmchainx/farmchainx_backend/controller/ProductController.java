package com.farmchainx.farmchainx_backend.controller;

import com.farmchainx.farmchainx_backend.model.*;
import com.farmchainx.farmchainx_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepo;
    private final FarmerRepository farmerRepo;

    // ── GET ALL PRODUCTS (marketplace) ──
    @GetMapping
    public ResponseEntity<List<Map<String,Object>>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String state) {

        List<Product> products;

        if (category != null && !category.isEmpty()) {
            products = productRepo
                    .findByActiveTrueAndCategoryOrderByAiTrustScoreDesc(
                            category);
        } else if (state != null && !state.isEmpty()) {
            products = productRepo.findByFarmerState(state);
        } else {
            products = productRepo
                    .findByActiveTrueOrderByAiTrustScoreDesc();
        }

        return ResponseEntity.ok(
                products.stream()
                        .map(this::toMap)
                        .collect(Collectors.toList()));
    }

    // ── GET FARMER'S PRODUCTS ──
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<Map<String,Object>>> getFarmerProducts(
            @PathVariable Long farmerId) {

        List<Product> products =
                productRepo.findByFarmerIdOrderByCreatedAtDesc(farmerId);

        return ResponseEntity.ok(
                products.stream()
                        .map(this::toMap)
                        .collect(Collectors.toList()));
    }

    // ── LIST NEW CROP ──
    @PostMapping
    public ResponseEntity<Map<String,Object>> listCrop(
            @RequestBody Map<String,Object> req) {

        Long farmerId = Long.valueOf(req.get("farmerId").toString());
        Farmer farmer = farmerRepo.findById(farmerId)
                .orElseThrow(() ->
                        new RuntimeException("Farmer not found"));

        Product product = Product.builder()
                .name((String) req.get("name"))
                .emoji((String) req.get("emoji"))
                .batchName((String) req.get("batchName"))
                .category((String) req.get("category"))
                .pricePerKg(Double.valueOf(
                        req.get("pricePerKg").toString()))
                .marketPrice(req.get("marketPrice") != null
                        ? Double.valueOf(req.get("marketPrice").toString())
                        : null)
                .quantityKg(Double.valueOf(
                        req.get("quantityKg").toString()))
                .certification((String) req.get("certification"))
                .aiTrustScore(90 + new Random().nextInt(8))
                .farmer(farmer)
                .build();

        Product saved = productRepo.save(product);
        return ResponseEntity.ok(toMap(saved));
    }

    // ── UPDATE ORDER STATUS ──
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Map<String,Object>> updateStock(
            @PathVariable Long id,
            @RequestBody Map<String,Object> req) {

        Product p = productRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        if (req.containsKey("quantityKg")) {
            p.setQuantityKg(Double.valueOf(
                    req.get("quantityKg").toString()));
            p.setOutOfStock(false);
            p.setActive(true);
        }

        return ResponseEntity.ok(toMap(productRepo.save(p)));
    }

    // ── UPLOAD VIDEO / QR ──
    @PostMapping("/{id}/upload-video")
    public ResponseEntity<Map<String,Object>> uploadVideo(
            @PathVariable Long id,
            @RequestParam MultipartFile video) throws Exception {

        Product p = productRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        String dir = "uploads/videos/";
        Files.createDirectories(Paths.get(dir));
        String filename = "product_" + id + "_"
                + System.currentTimeMillis() + ".mp4";
        video.transferTo(new File(dir + filename));

        p.setVideoPath(dir + filename);
        productRepo.save(p);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "videoPath", dir + filename,
                "qrCode", p.getQrCode()));
    }

    private Map<String,Object> toMap(Product p) {
        Map<String,Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("emoji", p.getEmoji());
        m.put("batchName", p.getBatchName());
        m.put("category", p.getCategory());
        m.put("pricePerKg", p.getPricePerKg());
        m.put("marketPrice", p.getMarketPrice());
        m.put("quantityKg", p.getQuantityKg());
        m.put("soldKg", p.getSoldKg());
        m.put("availableKg", p.getAvailableKg());
        m.put("certification", p.getCertification());
        m.put("aiTrustScore", p.getAiTrustScore());
        m.put("qrCode", p.getQrCode());
        m.put("active", p.getActive());
        m.put("outOfStock", p.getOutOfStock());
        if (p.getFarmer() != null) {
            m.put("farmerId", p.getFarmer().getId());
            m.put("farmerName", p.getFarmer().getName());
            m.put("farmerLocation",
                    p.getFarmer().getVillage() + ", "
                            + p.getFarmer().getState());
            m.put("farmerState", p.getFarmer().getState());
        }
        return m;
    }
}