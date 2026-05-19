package com.example.security;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TotpService {

    private static final String ISSUER = "Neo-4-Flix";
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    /**
     * Generate a new TOTP secret key.
     */
    public String generateSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    /**
     * Validate a TOTP code against a secret.
     */
    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }

    /**
     * Build the otpauth:// URI used to generate a QR code.
     * Format: otpauth://totp/Neo-4-Flix:{email}?secret={secret}&issuer=Neo-4-Flix
     */
    public String buildOtpAuthUri(String secret, String email) {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String encodedIssuer = URLEncoder.encode(ISSUER, StandardCharsets.UTF_8);
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                encodedIssuer, encodedEmail, secret, encodedIssuer);
    }
}
