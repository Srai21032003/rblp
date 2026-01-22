package com.internship.rblp.util;

import com.internship.rblp.models.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public final class JwtUtil {
    private static final String SECRET = "ThisIsASecretKeyThatIsDefinitelyLongEnoughForHMACSHA256!";
    private static final long EXPIRATION_TIME = 86400000; //24 hours

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public static String generateToken(UUID userId, Role role, String email){
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role",role.name())
                .claim("email",email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY)
                .compact();
    }

    public static Claims validateToken(String token){
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}