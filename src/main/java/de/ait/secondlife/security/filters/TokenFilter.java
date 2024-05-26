package de.ait.secondlife.security.filters;

import de.ait.secondlife.security.AuthInfo;
import de.ait.secondlife.security.services.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenFilter extends GenericFilterBean {

    public static final String COOKIE_ACCESS_TOKEN_NAME = "Access-Token";

    private static final String BEARER_HEADER_PREFIX = "Bearer ";
    private static final int BEARER_HEADER_PREFIX_LENGTH = BEARER_HEADER_PREFIX.length();

    private final TokenService service;

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {
        String token = getAccessTokenFromRequest((HttpServletRequest) servletRequest);
        if (token != null && service.validateAccessToken(token)) {
            Claims claims = service.getAccessClaims(token);
            AuthInfo authInfo = service.mapClaims(claims);
            authInfo.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(authInfo);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public String getAccessTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_ACCESS_TOKEN_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        String headerToken = request.getHeader("Authorization");
        if (headerToken != null && headerToken.startsWith(BEARER_HEADER_PREFIX)) {
            return headerToken.substring(BEARER_HEADER_PREFIX_LENGTH);
        }
        return null;
    }
}
