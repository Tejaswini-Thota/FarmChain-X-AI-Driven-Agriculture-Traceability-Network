package com.farmchainx.farmchainx_backend.controller;

import com.farmchainx.farmchainx_backend.model.*;
import com.farmchainx.farmchainx_backend.repository.*;
import com.farmchainx.farmchainx_backend.service.OtpService;
import com.farmchainx.farmchainx_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final FarmerRepository farmerRepo;
    private final ConsumerRepository consumerRepo;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    // ── SEND OTP ──
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String,Object>> sendOtp(
            @RequestBody Map<String,String> req) {

        String contact = req.get("contact");
        String role    = req.get("role");
        String name    = req.get("name");

        if (contact == null || role == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false,
                            "message", "Contact and role required"));
        }

        // For farmer — check if registered
        if ("FARMER".equals(role)) {
            boolean exists = farmerRepo.existsByEmail(contact);
            if (!exists) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false,
                                "message", "Email not registered. Please register first.",
                                "notRegistered", true));
            }
        }

        otpService.generateAndSendOtp(contact, role, name);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent to " + contact));
    }

    // ── VERIFY OTP & LOGIN ──
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String,Object>> verifyOtp(
            @RequestBody Map<String,String> req) {

        String contact = req.get("contact");
        String role    = req.get("role");
        String otp     = req.get("otp");

        boolean valid = otpService.verifyOtp(contact, role, otp);

        if (!valid) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false,
                            "message", "Invalid or expired OTP"));
        }

        String token = jwtUtil.generateToken(contact, role);

        // Get user data
        Map<String,Object> user = new HashMap<>();
        if ("FARMER".equals(role)) {
            farmerRepo.findByEmail(contact).ifPresent(f -> {
                user.put("id", f.getId());
                user.put("name", f.getName());
                user.put("email", f.getEmail());
                user.put("state", f.getState());
                user.put("district", f.getDistrict());
                user.put("village", f.getVillage());
                user.put("pincode", f.getPincode());
                user.put("acresOfLand", f.getAcresOfLand());
                user.put("languages", f.getLanguages());
                user.put("blockchainId", f.getBlockchainId());
            });
        } else {
            boolean isEmail = contact.contains("@");
            Optional<Consumer> c = isEmail
                    ? consumerRepo.findByEmail(contact)
                    : consumerRepo.findByMobile(contact);
            c.ifPresent(con -> {
                user.put("id", con.getId());
                user.put("name", con.getName());
                user.put("email", con.getEmail());
                user.put("mobile", con.getMobile());
                user.put("address", con.getAddress());
                user.put("city", con.getCity());
                user.put("state", con.getState());
                user.put("pincode", con.getPincode());
            });
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "role", role,
                "user", user));
    }

    // ── REGISTER FARMER ──
    @PostMapping("/register/farmer")
    public ResponseEntity<Map<String,Object>> registerFarmer(
            @RequestBody Map<String,Object> req) {

        String email = (String) req.get("email");

        if (farmerRepo.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false,
                            "message", "Email already registered"));
        }

        Farmer farmer = Farmer.builder()
                .name((String) req.get("name"))
                .email(email)
                .state((String) req.get("state"))
                .district((String) req.get("district"))
                .village((String) req.get("village"))
                .pincode((String) req.get("pincode"))
                .acresOfLand(
                        req.get("acresOfLand") != null
                                ? Double.valueOf(req.get("acresOfLand").toString())
                                : null)
                .languages((String) req.get("languages"))
                .build();

        Farmer saved = farmerRepo.save(farmer);
        String token = jwtUtil.generateToken(email, "FARMER");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Farmer registered successfully",
                "token", token,
                "user", Map.of(
                        "id", saved.getId(),
                        "name", saved.getName(),
                        "email", saved.getEmail(),
                        "blockchainId", saved.getBlockchainId()
                )));
    }

    // ── REGISTER / LOGIN CONSUMER ──
    @PostMapping("/register/consumer")
    public ResponseEntity<Map<String,Object>> registerConsumer(
            @RequestBody Map<String,String> req) {

        String name    = req.get("name");
        String contact = req.get("contact");
        boolean isEmail = contact.contains("@");

        // Check if exists — if yes just login
        Optional<Consumer> existing = isEmail
                ? consumerRepo.findByEmail(contact)
                : consumerRepo.findByMobile(contact);

        Consumer consumer = existing.orElseGet(() ->
                consumerRepo.save(Consumer.builder()
                        .name(name)
                        .email(isEmail ? contact : null)
                        .mobile(isEmail ? null : contact)
                        .build()));

        String token = jwtUtil.generateToken(contact, "CONSUMER");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "user", Map.of(
                        "id", consumer.getId(),
                        "name", consumer.getName(),
                        "email", consumer.getEmail() != null
                                ? consumer.getEmail() : "",
                        "mobile", consumer.getMobile() != null
                                ? consumer.getMobile() : ""
                )));
    }
}