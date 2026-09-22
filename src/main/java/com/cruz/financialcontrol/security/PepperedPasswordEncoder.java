package com.cruz.financialcontrol.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Class Name: PepperedPasswordEncoder
 * Description:
 *
 * @author edson
 * @date 16/09/2026
 */
@Primary
@Component
public class PepperedPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder passwordEncoder;
    private final SecretKeySpec pepperKey;

    public PepperedPasswordEncoder(@Qualifier("argon2PasswordEncoder") PasswordEncoder passwordEncoder, @Value("${security.password.pepper}") String pepperBase64) {
        this.passwordEncoder = passwordEncoder;
        this.pepperKey = new SecretKeySpec(Base64.getDecoder().decode(pepperBase64), "HmacSHA256");
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return passwordEncoder.encode(applyPepper(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(applyPepper(rawPassword), encodedPassword);
    }

    private String applyPepper(CharSequence rawPassword) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(pepperKey);
            byte[] out = mac.doFinal(rawPassword.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to apply pepper", e);
        }
    }
}
