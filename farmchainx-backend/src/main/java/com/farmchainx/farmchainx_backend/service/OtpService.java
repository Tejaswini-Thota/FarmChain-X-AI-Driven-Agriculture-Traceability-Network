package com.farmchainx.farmchainx_backend.service;

import com.farmchainx.farmchainx_backend.model.OtpStore;
import com.farmchainx.farmchainx_backend.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    @Value("${otp.from.email:thotatejaswini22@gmail.com}")
    private String fromEmail;

    public String generateAndSendOtp(
            String contact, String role, String name) {

        String otp = String.format("%06d",
                new Random().nextInt(999999));

        OtpStore store = OtpStore.builder()
                .contact(contact)
                .otpCode(otp)
                .role(role)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .attempts(0)
                .build();

        otpRepository.save(store);

        // Send email if contact is email
        if (contact.contains("@")) {
            sendOtpEmail(contact, otp, name);
        }

        return otp;
    }

    public boolean verifyOtp(
            String contact, String role, String code) {

        return otpRepository
                .findLatestOtp(contact, role)
                .map(otp -> {
                    if (otp.isValid(code)) {
                        otp.setUsed(true);
                        otpRepository.save(otp);
                        return true;
                    }
                    otp.setAttempts(otp.getAttempts() + 1);
                    otpRepository.save(otp);
                    return false;
                })
                .orElse(false);
    }

    private void sendOtpEmail(
            String toEmail, String otp, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(
                    "FarmChainX — Your OTP is " + otp);
            message.setText(
                    "Hello " + (name != null ? name : "User") + ",\n\n"
                            + "Your FarmChainX verification OTP is:\n\n"
                            + "  " + otp + "\n\n"
                            + "Valid for 5 minutes. Do not share this.\n\n"
                            + "— FarmChainX Team");
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println(
                    "Email send failed: " + e.getMessage());
        }
    }
}