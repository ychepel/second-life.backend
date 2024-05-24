package de.ait.secondlife.security.controllers;

import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.security.dto.AuthDto;
import de.ait.secondlife.security.dto.RefreshRequestDto;
import de.ait.secondlife.security.dto.TokenResponseDto;
import de.ait.secondlife.security.filters.TokenFilter;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.security.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication controller", description = "Controller for User and Admin authentication using JWT")
public class AuthController {

    private final AuthService service;

    @PostMapping("/{roleName}/login")
    @Operation(
            summary = "Login for Users and Admins",
            description = "Authenticates a user and returns a tokens. Access Token is valid for " + TokenService.ACCESS_TOKEN_EXPIRATION_DAYS + " days and Refresh Token is valid for " + TokenService.REFRESH_TOKEN_EXPIRATION_DAYS + " days."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful login",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponseDto.class))}
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or missing account", content = @Content),
            @ApiResponse(responseCode = "401", description = "Incorrect password", content = @Content)}
    )
    public ResponseEntity<TokenResponseDto> login(
            @Parameter(description = "Role name", schema = @Schema(implementation = Role.class))
            @PathVariable(name = "roleName") Role role,
            @RequestBody AuthDto authDto,
            HttpServletResponse response
    ) throws LoginException {
        TokenResponseDto tokenDto = service.login(role, authDto);
        setTokenToCookie(response, tokenDto.getAccessToken());
        return ResponseEntity.ok(tokenDto);
    }

    @PostMapping("/{roleName}/access")
    @Operation(summary = "Access update", description = "Recreation of JWT using Refresh Token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful grant of access",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TokenResponseDto.class))}
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)}
    )
    public ResponseEntity<TokenResponseDto> getNewAccessToken(
            @Parameter(description = "Role name", schema = @Schema(implementation = Role.class))
            @PathVariable(name = "roleName") Role role,
            @RequestBody RefreshRequestDto request,
            HttpServletResponse response
    ) throws LoginException {
        TokenResponseDto tokenDto = service.getAccessToken(role, request.getRefreshToken());
        setTokenToCookie(response, tokenDto.getAccessToken());
        return ResponseEntity.ok(tokenDto);
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
