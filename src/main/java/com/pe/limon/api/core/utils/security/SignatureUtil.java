package com.pe.limon.api.core.utils.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;


@Component
public class SignatureUtil {
    private static final String HMAC_ALGO = "HmacSHA256";

    @Value("${security.signature.secret.key}")
    private String SECRET_KEY; // mover a env

    public String sign(String code) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec =
                    new SecretKeySpec(SECRET_KEY.getBytes(), HMAC_ALGO);
            mac.init(keySpec);

            byte[] raw = mac.doFinal(code.getBytes());

            // Base64 URL safe (ideal para QR)
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(raw);

        } catch (Exception e) {
            throw new RuntimeException("Error generating QR signature", e);
        }
    }

    public boolean verify(String token, String signature) {
        String expected = sign(token);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        );
    }
}
