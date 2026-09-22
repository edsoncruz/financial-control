package com.cruz.financialcontrol.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;

/**
 * Service class for handling JWT token generation and validation.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generates a JWT token for the given user details.
     * @param userDetails user details containing the username and authorities
     * @return a signed JWT token as a String
     */
    public String generateToken(UserDetails userDetails) {

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(jwtExpiration)))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Validates the given JWT token and returns the username if valid.
     * @param token the JWT token to validate
     * @return the username extracted from the token if valid, otherwise throws an JwtException
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     * @throws IllegalArgumentException if the token is null or empty
     */
    public String validateToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    /**
     * Parses the claims from the given JWT token.
     * @param token the JWT token to parse
     * @return the Claims object containing the token's claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     * @throws IllegalArgumentException if the token is null or empty
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Retrieves the signing key used for JWT token generation and validation.
     * @return the SecretKey used for signing and verifying JWT tokens
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}