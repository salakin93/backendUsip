package edu.usip.pdfdocumentmanager.security;

import edu.usip.pdfdocumentmanager.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, Set<Role> roles) {
        var rolesAsString = roles.stream().map(Role::name).collect(Collectors.toList());
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", rolesAsString)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String extractUsername(String token) {
        return validateToken(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<Role> extractRoles(String token) {
        var roles = (java.util.List<String>) validateToken(token).get("roles");
        return roles.stream().map(Role::valueOf).collect(Collectors.toSet());
    }
}
