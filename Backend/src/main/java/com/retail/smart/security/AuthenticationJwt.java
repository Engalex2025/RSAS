package com.retail.smart.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class AuthenticationJwt {

    // Fixed secure key (≥ 512 bits)
    private static final String SECRET = "zNs93vPq2Whd7KxJu84GdPt3LzqTAxEojFnvPqZrQhVkYdRpZkTfGhMqNjRgSmUp";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final long EXPIRATION_TIME_MS = 24 * 60 * 60 * 1000; // 24 hours
    private static final String ROLES_CLAIM = "roles";

    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim(ROLES_CLAIM, roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token, String username) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject().equals(username) && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        return getClaimsFromToken(token).get(ROLES_CLAIM, List.class);
    }

    public Claims getClaimsFromToken(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
}


    // Optional setter for test injection
    void setSecretKey(SecretKey key) {
        // Not needed in production
    }
}
