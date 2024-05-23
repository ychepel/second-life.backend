package de.ait.secondlife.security.services;

import de.ait.secondlife.models.AuthenticatedUser;
import de.ait.secondlife.security.AuthInfo;
import de.ait.secondlife.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class TokenService {

    public static final int ACCESS_TOKEN_EXPIRATION_DAYS = 7;
    public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 30;

    private static final String USER_ROLE_VARIABLE_NAME = "roles";
    private static final String USER_EMAIL_VARIABLE_NAME = "email";

    private SecretKey accessKey;
    private SecretKey refreshKey;

    public TokenService(@Value("${key.access}") String accessKey, @Value("${key.refresh}") String refreshKey) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
    }

    public String generateAccessToken(AuthenticatedUser user) {
        LocalDateTime currentTime = LocalDateTime.now();
        Instant expirationInstant = currentTime
                .plusDays(ACCESS_TOKEN_EXPIRATION_DAYS)
                .atZone(ZoneId.systemDefault())
                .toInstant();
        Date expirationDate = Date.from(expirationInstant);

        return Jwts.builder()
                .subject(user.getEmail())
                .expiration(expirationDate)
                .signWith(accessKey)
                .claim(USER_ROLE_VARIABLE_NAME, user.getAuthorities())
                .claim(USER_EMAIL_VARIABLE_NAME, user.getEmail())
                .compact();
    }

    public String generateRefreshToken(AuthenticatedUser user) {
        LocalDateTime currentTime = LocalDateTime.now();
        Instant expirationInstant = currentTime
                .plusDays(REFRESH_TOKEN_EXPIRATION_DAYS)
                .atZone(ZoneId.systemDefault())
                .toInstant();
        Date expirationDate = Date.from(expirationInstant);

        return Jwts.builder()
                .subject(user.getEmail())
                .expiration(expirationDate)
                .signWith(refreshKey)
                .compact();
    }

    public boolean validateAccessToken(String accessToken) {
        return validateToken(accessToken, accessKey);
    }

    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, refreshKey);
    }

    private boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getAccessClaims(String accessToken) {
        return getClaims(accessToken, accessKey);
    }

    public Claims getRefreshClaims(String refreshToken) {
        return getClaims(refreshToken, refreshKey);
    }

    private Claims getClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AuthInfo mapClaims(Claims claims) {
        String userEmail = claims.getSubject();
        List<String> roleList = (List<String>) claims.get(USER_ROLE_VARIABLE_NAME);
        Set<Role> roles = new HashSet<>();
        for (String role : roleList) {
            roles.add(Role.valueOf(role));
        }
        return new AuthInfo(userEmail, roles);
    }
}
