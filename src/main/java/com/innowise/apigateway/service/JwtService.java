package com.innowise.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    private static final String ACCESS_TOKEN_TYPE = "accessToken";

    public JwtService(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public boolean isAccessTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);

            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            Date expiration = claims.getExpiration();

            return ACCESS_TOKEN_TYPE.equals(tokenType)
                    && expiration != null
                    && expiration.after(new Date());

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
