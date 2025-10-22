package com.pubsystem.auth.security;

import com.pubsystem.auth.util.KeyGenerator;
import io.jsonwebtoken.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            System.out.println("=".repeat(60));
            System.out.println("Initializing JwtTokenProvider");
            System.out.println("Private key resource: " + privateKeyResource.getDescription());
            System.out.println("Public key resource: " + publicKeyResource.getDescription());

            // ✅ FIXED: Use InputStream instead of File for JAR compatibility
            // This works in both IntelliJ (filesystem) and Docker (JAR)
            try (InputStream privateKeyStream = privateKeyResource.getInputStream();
                 InputStream publicKeyStream = publicKeyResource.getInputStream()) {

                privateKey = KeyGenerator.loadPrivateKeyFromStream(privateKeyStream);
                publicKey = KeyGenerator.loadPublicKeyFromStream(publicKeyStream);
            }

            System.out.println("✅ JWT keys loaded successfully");
            System.out.println("Private key algorithm: " + privateKey.getAlgorithm());
            System.out.println("Public key algorithm: " + publicKey.getAlgorithm());
            System.out.println("=".repeat(60));

        } catch (Exception e) {
            System.err.println("=".repeat(60));
            System.err.println("❌ CRITICAL ERROR: Failed to load RSA keys");
            System.err.println("Private key: " + privateKeyResource.getDescription());
            System.err.println("Public key: " + publicKeyResource.getDescription());
            System.err.println("Error: " + e.getMessage());
            System.err.println("=".repeat(60));
            e.printStackTrace();
            throw new RuntimeException("Failed to load RSA keys", e);
        }
    }

    /**
     * Generate JWT token with RSA signature
     */
    public String generateToken(Authentication authentication) {
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userPrincipal.getEmail())
                .claim("userId", userPrincipal.getId())
                .claim("name", userPrincipal.getName())
                .claim("roles", roles)
                .claim("authProvider", userPrincipal.getAuthProvider())
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Generate token from user ID and email (for OAuth2)
     */
    public String generateTokenFromEmail(String email, Long userId, String name, String roles, String authProvider) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("name", name)
                .claim("roles", roles)
                .claim("authProvider", authProvider)
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Extract email from JWT token
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Extract user ID from JWT token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("userId", Long.class);
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException ex) {
            System.err.println("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty");
        }
        return false;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }
}