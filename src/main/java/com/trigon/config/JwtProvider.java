package com.trigon.config;

import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
@Component
public class JwtProvider {

    // Remove this field!
    // SecretKey key=Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(JwtConstant.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication auth) {
        SecretKey key = getSigningKey();

        String authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        String jwt = Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 846_000_000))
                .claim("email", auth.getName())
                .claim("authorities", authorities)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println(jwt);
        return jwt;
    }

    public String getMailFromToken(String jwt) {
        try {
        			  
        	if (jwt != null && jwt.startsWith("Bearer ")) {
                jwt = jwt.substring(7);
            }
		 
            SecretKey key = getSigningKey();
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
            return String.valueOf(claims.get("email"));
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT", e);
        }
    }
}

