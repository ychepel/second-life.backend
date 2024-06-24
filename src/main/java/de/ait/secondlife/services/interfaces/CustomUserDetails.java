package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetails extends UserDetailsService {

    UserDetails loadUserByUsername(String username);

    void updateLastActive(User user);
}
