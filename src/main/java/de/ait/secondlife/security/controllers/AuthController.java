package de.ait.secondlife.security.controllers;

import de.ait.secondlife.security.filters.TokenFilter;
import de.ait.secondlife.security.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import de.ait.secondlife.security.dto.AuthDto;
import de.ait.secondlife.security.dto.RefreshRequestDto;
import de.ait.secondlife.security.dto.TokenResponseDto;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.security.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication controller", description = "Controller for User and Admin authentication using JWT")
public class AuthController {

    private final AuthService service;

    @PostMapping("/{roleName}/login")
    @Operation(summary = "Login for Users and Admins", description = "Authenticates a user and returns a tokens. Access Token is valid for " + TokenService.ACCESS_TOKEN_EXPIRATION_DAYS + " days and Refresh Token is valid for " + TokenService.REFRESH_TOKEN_EXPIRATION_DAYS + " days.")
    public ResponseEntity<Object> login(
            @Parameter(description = "Role name", schema = @Schema(implementation = Role.class))
            @PathVariable(name = "roleName") Role role,
            @RequestBody AuthDto authDto,
            HttpServletResponse response
    ) {
        try {
            TokenResponseDto tokenDto = service.login(role, authDto);
            setTokenToCookie(response, tokenDto.getAccessToken());
            return ResponseEntity.ok(tokenDto);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{roleName}/access")
    @Operation(summary = "Access update", description = "Recreation of JWT using Refresh Token")
    public ResponseEntity<Object> getNewAccessToken(
            @Parameter(description = "Role name", schema = @Schema(implementation = Role.class))
            @PathVariable(name = "roleName") Role role,
            @RequestBody RefreshRequestDto request,
            HttpServletResponse response
    ) {
        try {
            TokenResponseDto tokenDto = service.getAccessToken(role, request.getRefreshToken());
            setTokenToCookie(response, tokenDto.getAccessToken());
            return ResponseEntity.ok(tokenDto);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{roleName}/logout")
    @Operation(summary = "Logout", description = "Logout User/Admin from application")
    public void logout(
            @Parameter(description = "Role name", schema = @Schema(implementation = Role.class))
            @PathVariable(name = "roleName") Role role,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        service.logout(request, role);
        setTokenToCookie(response, null);
    }

    private void setTokenToCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie(TokenFilter.COOKIE_ACCESS_TOKEN_NAME, accessToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        if (accessToken == null) {
            cookie.setMaxAge(0);
        }
        response.addCookie(cookie);
    }

}
