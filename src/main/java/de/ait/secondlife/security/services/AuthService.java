package de.ait.secondlife.security.services;

import de.ait.secondlife.domain.entity.Admin;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotActiveException;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthenticatedException;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthorizedException;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.security.dto.AuthDto;
import de.ait.secondlife.security.dto.TokenResponseDto;
import de.ait.secondlife.security.filters.TokenFilter;
import de.ait.secondlife.services.interfaces.CustomAdminDetails;
import de.ait.secondlife.services.interfaces.CustomUserDetails;
import de.ait.secondlife.services.interfaces.UserService;
import io.jsonwebtoken.Claims;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing user authentication and authorization.
 * Provides methods for logging in, retrieving the currently authenticated user,
 * getting the current user's role, and logging out.
 *
 * <p>
 * This class interacts with various services to perform authentication and authorization tasks.
 * It uses lazy loading for dependencies to avoid circular dependencies during bean initialization.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown by this class include:
 * <ul>
 *     <li>{@link UserIsNotAuthenticatedException} - if the user is not authenticated</li>
 *     <li>{@link UserIsNotAuthorizedException} - if the user is not authorized</li>
 *     <li>{@link UserIsNotActiveException} - if the user's account is not active</li>
 *     <li>{@link AccountNotFoundException} - if the user account is not found</li>
 *     <li>{@link AccountException} - if there is an issue with the user account</li>
 *     <li>{@link AuthException} - if there is a general authentication issue</li>
 * </ul>
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 * @author: Second Life Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomAdminDetails adminDetailsService;
    private final TokenService tokenService;
    private final TokenFilter tokenFilter;
    private final BCryptPasswordEncoder encoder;
    private final CustomUserDetails userDetailsService;

    private UserService userService;

    /**
     * Sets the UserService with lazy initialization.
     *
     * @param userService the service for managing users.
     */
      @Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    private final Map<String, String> refreshStorage = new HashMap<>();

    /**
     * Retrieves the currently authenticated user if they have the ROLE_USER role.
     *
     * @return the currently authenticated user.
     * @throws CredentialException if there are issues with the user's credentials.
     */
    public static User getCurrentUser() throws CredentialException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            throw new UserIsNotAuthenticatedException();
        }
        if (authenticatedUser.getRole() != Role.ROLE_USER) {
            throw new UserIsNotAuthorizedException();
        }
        User user = (User) authenticatedUser;
        if (!user.isActive()) {
            throw new UserIsNotActiveException();
        }
        return user;
    }

    /**
     * Retrieves the currently authenticated user if they have the ROLE_ADMIN role.
     *
     * @return the currently authenticated admin.
     * @throws CredentialException if there are issues with the user's credentials.
     */
    public static Admin getCurrentAdmin() throws CredentialException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            throw new UserIsNotAuthenticatedException();
        }
        if (authenticatedUser.getRole() != Role.ROLE_ADMIN) {
            throw new UserIsNotAuthorizedException();
        }
        return (Admin) authenticatedUser;
    }

    /**
     * Retrieves the role of the currently authenticated user.
     *
     * @return the role of the currently authenticated user.
     */
   public static Role getCurrentRole() {
        try {
            AuthenticatedUser authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                throw new UserIsNotAuthenticatedException();
            }
            return authenticatedUser.getRole();
        } catch (CredentialException e) {
            return null;
        }
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return the authenticated user.
     * @throws CredentialException if there are issues with the user's credentials.
     */
    private static AuthenticatedUser getAuthenticatedUser() throws CredentialException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if ("anonymousUser".equals(principal)) {
            throw new UserIsNotAuthenticatedException();
        }
        return (AuthenticatedUser) principal;
    }

    /**
     * Logs in the user with the given role and credentials.
     *
     * @param role the role of the user.
     * @param authDto the authentication data transfer object containing user credentials.
     * @return a TokenResponseDto containing the access and refresh tokens.
     * @throws LoginException if there is an error during login.
     */
    public TokenResponseDto login(Role role, AuthDto authDto) throws LoginException {
        String userEmail = authDto.getEmail();
        AuthenticatedUser foundUser = getAuthenticatedUser(role, userEmail);

        if (encoder.matches(authDto.getPassword(), foundUser.getPassword())) {
            String accessToken = tokenService.generateAccessToken(foundUser);
            String refreshToken = tokenService.generateRefreshToken(foundUser);
            refreshStorage.put(getTokenStorageKey(role, userEmail), refreshToken);
            return new TokenResponseDto(foundUser.getId(), accessToken, refreshToken);
        } else {
            throw new CredentialException("Password is incorrect");
        }
    }

    /**
     * Retrieves a new access token using the given refresh token.
     *
     * @param role the role of the user.
     * @param inboundRefreshToken the refresh token.
     * @return a TokenResponseDto containing the new access token.
     * @throws LoginException if there is an error during token retrieval.
     */
    public TokenResponseDto getAccessToken(Role role, @NonNull String inboundRefreshToken) throws LoginException {
        if (tokenService.validateRefreshToken(inboundRefreshToken)) {
            Claims refreshClaims = tokenService.getRefreshClaims(inboundRefreshToken);
            String userEmail = refreshClaims.getSubject();
            String storedRefreshToken = refreshStorage.get(getTokenStorageKey(role, userEmail));

            if (inboundRefreshToken.equals(storedRefreshToken)) {
                AuthenticatedUser user = getAuthenticatedUser(role, userEmail);
                String accessToken = tokenService.generateAccessToken(user);
                return new TokenResponseDto(user.getId(), accessToken, inboundRefreshToken);
            }
        }
        throw new AuthException("Refresh token is incorrect");
    }

    /**
     * Retrieves the authenticated user by their ID and role.
     *
     * @param role the role of the user.
     * @param userId the ID of the user.
     * @return the authenticated user.
     * @throws AuthException if there is an error during user retrieval.
     */
    public AuthenticatedUser getAuthenticatedUser(Role role, Long userId) throws AuthException {
        if (role == Role.ROLE_ADMIN) {
            return adminDetailsService.findById(userId);
        }
        if (role == Role.ROLE_USER) {
            return userService.findById(userId);
        }

        throw new AuthException("Undefined role");
    }

    /**
     * Retrieves the authenticated user by their email and role.
     *
     * @param role the role of the user.
     * @param userEmail the email of the user.
     * @return the authenticated user.
     * @throws LoginException if there is an error during user retrieval.
     */
    private AuthenticatedUser getAuthenticatedUser(Role role, String userEmail) throws LoginException {
        if (role == Role.ROLE_ADMIN) {
            try {
                return (AuthenticatedUser) adminDetailsService.loadUserByUsername(userEmail);
            } catch (UsernameNotFoundException e) {
                throw new AccountNotFoundException("Admin not found");
            }
        }

        if (role == Role.ROLE_USER) {
            AuthenticatedUser authenticatedUser;
            try {
                authenticatedUser = (AuthenticatedUser) userDetailsService.loadUserByUsername(userEmail);
            } catch (UsernameNotFoundException e) {
                throw new AccountNotFoundException("User not found");
            }
            if (!authenticatedUser.isEnabled()) {
                throw new AccountException("User is not active");
            }
            return authenticatedUser;
        }

        throw new AuthException("Undefined role");
    }

    /**
     * Logs out the user by removing their refresh token from storage.
     *
     * @param request the HTTP request containing the access token.
     * @param role the role of the user.
     */
    public void logout(HttpServletRequest request, Role role) {
        String accessToken = tokenFilter.getAccessTokenFromRequest(request);
        if (accessToken != null && tokenService.validateAccessToken(accessToken)) {
            Claims accessClaims = tokenService.getAccessClaims(accessToken);
            String userEmail = accessClaims.getSubject();
            refreshStorage.remove(getTokenStorageKey(role, userEmail));
        }
    }

    /**
     * Generates a key for storing refresh tokens based on the user's role and email.
     *
     * @param role the role of the user.
     * @param email the email of the user.
     * @return the key for storing the refresh token.
     */
    private String getTokenStorageKey(Role role, String email) {
        return role + ":" + email;
    }
}
