package de.ait.secondlife.security.services;

import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
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

/**
 * Service for managing JWT tokens.
 * Provides methods for generating, validating, and extracting claims from access and refresh tokens.
 *
 * <p>
 * This class uses HMAC SHA keys for signing and verifying JWT tokens. The keys are provided through configuration properties.
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 * @version 1.0
 */
@Service
public class TokenService {

    public static final int ACCESS_TOKEN_EXPIRATION_DAYS = 7;
    public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 30;

    private static final String USER_ROLE_VARIABLE_NAME = "roles";
    private static final String USER_EMAIL_VARIABLE_NAME = "email";

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    /**
     * Constructs a TokenService with the specified access and refresh keys.
     *
     * @param accessKey  the access key for signing access tokens.
     * @param refreshKey the refresh key for signing refresh tokens.
     */
    public TokenService(@Value("${key.access}") String accessKey, @Value("${key.refresh}") String refreshKey) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
    }

    /**
     * Generates an access token for the specified user.
     *
     * @param user the authenticated user.
     * @return the generated access token.
     */
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

    /**
     * Generates a refresh token for the specified user.
     *
     * @param user the authenticated user.
     * @return the generated refresh token.
     */
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

    /**
     * Validates the specified access token.
     *
     * @param accessToken the access token to validate.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateAccessToken(String accessToken) {
        return validateToken(accessToken, accessKey);
    }

    /**
     * Validates the specified refresh token.
     *
     * @param refreshToken the refresh token to validate.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, refreshKey);
    }

    /**
     * Validates the specified token with the given key.
     *
     * @param token the token to validate.
     * @param key   the key to use for validation.
     * @return true if the token is valid, false otherwise.
     */
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

    /**
     * Retrieves claims from the specified access token.
     *
     * @param accessToken the access token.
     * @return the claims contained in the token.
     */
    public Claims getAccessClaims(String accessToken) {
        return getClaims(accessToken, accessKey);
    }

    /**
     * Retrieves claims from the specified refresh token.
     *
     * @param refreshToken the refresh token.
     * @return the claims contained in the token.
     */
    public Claims getRefreshClaims(String refreshToken) {
        return getClaims(refreshToken, refreshKey);
    }

    /**
     * Retrieves claims from the specified token with the given key.
     *
     * @param token the token.
     * @param key   the key to use for retrieving claims.
     * @return the claims contained in the token.
     */
    private Claims getClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Maps the claims to an AuthInfo object.
     *
     * @param claims the claims to map.
     * @return the AuthInfo object containing the mapped information.
     */
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
