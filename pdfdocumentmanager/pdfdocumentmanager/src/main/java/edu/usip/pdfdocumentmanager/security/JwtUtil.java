package edu.usip.pdfdocumentmanager.security;

import edu.usip.pdfdocumentmanager.model.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final String secret;
    private final long expirationMs;
    private Key key;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    @PostConstruct
    void init() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret debe tener al menos 32 caracteres (HS256).");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Set<Role> roles) {
        List<String> rolesAsString = roles.stream()
                .map(Role::name)
                .collect(Collectors.toList());

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", rolesAsString)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return validateToken(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<Role> extractRoles(String token) {
        Object raw = validateToken(token).get("roles");
        if (raw == null) return Set.of();

        List<String> roles = (List<String>) raw;
        return roles.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}
