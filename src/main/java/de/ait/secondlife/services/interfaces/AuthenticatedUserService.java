package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.security.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthenticatedUserService extends UserDetailsService {

    static Role getAuthenticatedUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return (Role) authentication
                .getAuthorities()
                .stream()
                .findFirst()
                .orElse(null);
    }
}
