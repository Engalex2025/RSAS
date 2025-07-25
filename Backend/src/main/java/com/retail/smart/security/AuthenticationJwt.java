package com.retail.smart.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class AuthenticationJwt {

    // Secure key (must be >= 512 bits for HS512)
    private SecretKey secretKey;

    private static final long EXPIRATION_TIME_MS = 60 * 60 * 1000; // 1 hour
    private static final String ROLES_CLAIM = "roles";

    // Generate a safe HS512-compatible key at runtime
    @PostConstruct
    public void init() {
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    }

    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim(ROLES_CLAIM, roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(secretKey)
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

    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Setter for test injection (allows fixed secret key during unit tests)
    void setSecretKey(SecretKey key) {
        this.secretKey = key;
    }
}
