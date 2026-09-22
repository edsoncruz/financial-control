package com.cruz.financialcontrol.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // 256-bit random key, base64-encoded — matches what JwtService.getSignInKey() expects to decode.
    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString("this-is-a-test-secret-key-32bytes!!".getBytes());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3_600_000L); // 1 hour

        userDetails = User.withUsername("user@example.com")
                .password("irrelevant")
                .authorities("USER")
                .build();
    }

    @Test
    void generateToken_shouldProduceNonBlankToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    void validateToken_shouldReturnUsernameEmbeddedAtGeneration() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.validateToken(token);

        assertThat(username).isEqualTo("user@example.com");
    }
}
