package de.ait.secondlife.security.services;

import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import de.ait.secondlife.security.dto.AuthDto;
import de.ait.secondlife.security.dto.TokenResponseDto;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.security.filters.TokenFilter;
import de.ait.secondlife.services.interfaces.AdminService;
import de.ait.secondlife.services.interfaces.UserService;
import io.jsonwebtoken.Claims;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminService adminService;
    private final UserService userService;
    private final TokenService tokenService;
    private final TokenFilter tokenFilter;
    private final BCryptPasswordEncoder encoder;

    private Map<String, String> refreshStorage = new HashMap<>();

    public TokenResponseDto login(Role role, AuthDto authDto) throws LoginException {
        String userEmail = authDto.getEmail();
        AuthenticatedUser foundUser = getAuthenticatedUser(role, userEmail);

        if (encoder.matches(authDto.getPassword(), foundUser.getPassword())) {
            String accessToken = tokenService.generateAccessToken(foundUser);
            String refreshToken = tokenService.generateRefreshToken(foundUser);
            refreshStorage.put(getTokenStorageKey(role, userEmail), refreshToken);
            return new TokenResponseDto(accessToken, refreshToken);
        } else {
            throw new CredentialException("Password is incorrect");
        }
    }

    public TokenResponseDto getAccessToken(Role role, @NonNull String inboundRefreshToken) throws LoginException {
        if (tokenService.validateRefreshToken(inboundRefreshToken)) {
            Claims refreshClaims = tokenService.getRefreshClaims(inboundRefreshToken);
            String userEmail = refreshClaims.getSubject();
            String storedRefreshToken = refreshStorage.get(getTokenStorageKey(role, userEmail));

            if (inboundRefreshToken.equals(storedRefreshToken)) {
                AuthenticatedUser user = getAuthenticatedUser(role, userEmail);
                String accessToken = tokenService.generateAccessToken(user);
                return new TokenResponseDto(accessToken, inboundRefreshToken);
            }
        }
        throw new AuthException("Refresh token is incorrect");
    }

    private AuthenticatedUser getAuthenticatedUser(Role role, String userEmail) throws LoginException {
        if (role == Role.ROLE_ADMIN) {
            try {
                return (AuthenticatedUser) adminService.loadUserByUsername(userEmail);
            } catch (UsernameNotFoundException e) {
                throw new AccountNotFoundException("Admin not found");
            }
        }

        if (role == Role.ROLE_USER) {
            AuthenticatedUser authenticatedUser;
            try {
                authenticatedUser = (AuthenticatedUser) userService.loadUserByUsername(userEmail);
            } catch (UsernameNotFoundException e) {
                throw new AccountNotFoundException("User not found");
            }
            if (!authenticatedUser.isEnabled()) {
                throw new AccountException("User is not active.");
            }
            return authenticatedUser;
        }

        throw new AuthException("Undefined role");
    }

    public void logout(HttpServletRequest request, Role role) {
        String accessToken = tokenFilter.getAccessTokenFromRequest(request);
        if (accessToken != null && tokenService.validateAccessToken(accessToken)) {
            Claims accessClaims = tokenService.getAccessClaims(accessToken);
            String userEmail = accessClaims.getSubject();
            refreshStorage.remove(getTokenStorageKey(role, userEmail));
        }
    }

    private String getTokenStorageKey(Role role, String email) {
        return role + ":" + email;
    }
}
